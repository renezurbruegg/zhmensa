package com.mensa.zhmensa.models;

import android.util.Log;

import androidx.annotation.Nullable;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.HttpUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.msebera.android.httpclient.Header;

public class UzhMensaCategory extends MensaCategory {

    private static final MensaApiRoute[] ROUTES = {
            new MensaApiRoute(147, "Untere Mensa A", Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(148, "Obere Mensa B", Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(150, "Lichthof", Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(180, "Irchel",Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(146, "Tierspital",Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(151, "Zentrum Für Zahnmedizin",Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(143, "Platte",Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(346, "Rämi 59 (vegan)",Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(149, "Untere Mensa A",Mensa.MenuCategory.DINNER),
            new MensaApiRoute(256, "Irchel",Mensa.MenuCategory.DINNER),
    };
    /**
     *         "UZH untere Mensa A": 147,
     *         "UZH obere Mensa B": 148,
     *         "UZH Lichthof": 150,
     *         "UZH Irchel": 180,
     *         "UZH Tierspital": 146,
     *         "UZH Zentrum Für Zahnmedizin": 151,
     *         "UZH Platte": 143,
     *          "UZH Rämi 59 (vegan)": 346,
     *            "UZH untere Mensa A (abend)": 149,
     * "UZH Irchel (abend)": 256
     */

    public UzhMensaCategory(String displayName) {
        super(displayName);
    }

    public UzhMensaCategory(String displayName, List<String> knownMensaIds) {
        super(displayName, knownMensaIds);
    }


    @Override
    public List<MensaListObservable> loadMensasFromAPI() {
        List<MensaListObservable> obsList = new ArrayList<>();
        for(Mensa.Weekday day: Mensa.Weekday.values()) {

            for (MensaApiRoute route : ROUTES) {
                final MensaListObservable obs = new MensaListObservable(day, route.mealType);
                obsList.add(obs);
                HttpUtils.getByUrl("http://zfv.ch/de/menus/rssMenuPlan?menuId=" + route.id + "&&dayOfWeek=" + (day.day + 1), new RequestParams(), new XMLResponseHandler(obs, route));
            }
        }
        return obsList;
    }



    private static class MensaApiRoute {
        public int id;
        public String name;
        Mensa.MenuCategory mealType;

        MensaApiRoute(int id, String name, Mensa.MenuCategory mealType) {
            this.mealType = mealType;
            this.id = id;
            this.name = name;
        }
    }

    private class XMLResponseHandler extends AsyncHttpResponseHandler {
        private final MensaListObservable observable;
        private final MensaApiRoute apiRoute;

        XMLResponseHandler(MensaListObservable observable, MensaApiRoute apiRoute) {
            this.observable = observable;
            this.apiRoute = apiRoute;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
       //     Log.e("reposne", new String(responseBody));
            try {
                Mensa m = parseXML(apiRoute.name, responseBody, observable.day, observable.mealType);

                observable.addNewMensa(m);

            } catch (IOException e) {
                Log.e("UZHMensa.parse", "could not parse string: " + new String(responseBody) + " error: " + e.getMessage());
                e.printStackTrace();
            } catch (SAXException e) {
                Log.e("UZHMensa.parse", "could not parse string: " + new String(responseBody) + " error: " + e.getMessage());
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                Log.e("UZHMensa.parse", "could not parse string: " + new String(responseBody) + " error: " + e.getMessage());
                e.printStackTrace();
            }

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.e("fail", error.getMessage());
            Log.e("response: ", new String(responseBody));
            Log.e("id:", apiRoute.id+ "");

            error.printStackTrace();
        }


        private Mensa parseXML(String name, byte[] xmlFile, Mensa.Weekday day, Mensa.MenuCategory mealType) throws  IOException, SAXException, ParserConfigurationException {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(xmlFile));
            Element element=doc.getDocumentElement();
            element.normalize();
            NodeList nList = doc.getElementsByTagName("summary");
            Node summaryNode = nList.item(0);

            for (int i = 0; i < summaryNode.getChildNodes().getLength(); i++) {
                Node n = summaryNode.getChildNodes().item(i);
                if(n.getNodeName().equals("div")) {
                    // Found a new Mensa. Mensa are always in first div element
                    Mensa m = new Mensa(name, name);
                    m.setMenuForDayAndCategory(day,mealType,getMensaFromDivNode(name, mealType, n));
                    return m;
                }
            }

            return new Mensa("Error no div node found", "ERROR");
        }


        private String domAsString(Node rootnode) {
            StringBuilder retString = new StringBuilder(trimString(rootnode.getNodeValue()) + (rootnode.getNodeName().equals("#text") ? " " : "\n"));
            NodeList children = rootnode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                retString.append(domAsString(children.item(i)));
            }

            return retString.toString();
        }
        private String trimString(String stringToTrim) {
            if(stringToTrim == null) {
                return "";
            }
            return stringToTrim.trim();
        }

        private List<IMenu> getMensaFromDivNode(String mensaName, Mensa.MenuCategory mealType, Node divNode) {
            List<IMenu> menuList = new ArrayList<>();

            Menu currentMenu = null;
            for (int i = 0; i < divNode.getChildNodes().getLength(); i++) {
                Node n = divNode.getChildNodes().item(i);

                if(n.getNodeName().equals("h3")) {
                    // Found Begin of menu
                    currentMenu = new Menu(null, null,null,null,null);
                    menuList.add(currentMenu);

                    // found new Meal
                    NodeList children = n.getChildNodes();
                    String name = trimString(children.item(0).getNodeValue());
                    currentMenu.setName(name);
                    currentMenu.setId(Helper.getIdForMenu(mensaName, name, menuList.size() , mealType));
                    currentMenu.setPrices(trimString(children.item(1).getFirstChild().getNodeValue()));
                }

                if(n.getNodeName().equals("p")) {
                    if(currentMenu == null)
                        continue;
                    // Found Description
                    currentMenu.setDescription(domAsString(n));
                }
            }
            return menuList;
        }

    }


    @Nullable
    @Override
    public Integer getCategoryIconId() {
        return R.drawable.ic_uni;
    }
}
