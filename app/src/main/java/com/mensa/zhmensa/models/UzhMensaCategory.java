package com.mensa.zhmensa.models;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mensa.zhmensa.services.HttpUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Observable;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.msebera.android.httpclient.Header;

public class UzhMensaCategory extends MensaCategory {

    private static final MensaApiRoute[] ROUTES = {
            new MensaApiRoute(147, "UZH untere Mensa A", Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(148, "UZH obere Mensa B", Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(150, "UZH Lichthof", Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(180, "UZH Irchel",Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(146, "ZH Tierspital",Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(151, "UZH Zentrum Für Zahnmedizin",Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(143, "ZH Platte",Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(346, "UZH Rämi 59 (vegan)",Mensa.MenuCategory.LUNCH),
            new MensaApiRoute(149, "UZH untere Mensa A",Mensa.MenuCategory.DINNER),
            new MensaApiRoute(256, "UZH Irchel",Mensa.MenuCategory.DINNER),
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
    private final Set<Integer> servedRoutes = new HashSet<>();

    public UzhMensaCategory(String displayName) {
        super(displayName);
    }

    @Override
    Observable getMensaUpdateForDayAndMeal(Mensa.Weekday day, Mensa.MenuCategory menuCategory) {
        return null;
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
        public Mensa.MenuCategory mealType;

        public MensaApiRoute(int id, String name, Mensa.MenuCategory mealType) {
            this.mealType = mealType;
            this.id = id;
            this.name = name;
        }
    }
    private class XMLResponseHandler extends AsyncHttpResponseHandler {
        private final MensaListObservable observable;
        private final MensaApiRoute apiRoute;

        public XMLResponseHandler(MensaListObservable observable, MensaApiRoute apiRoute) {
            this.observable = observable;
            this.apiRoute = apiRoute;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
       //     Log.e("reposne", new String(responseBody));
            try {
                Mensa m = parseXML(apiRoute.name, responseBody, observable.day, observable.mealType);
               // Log.e("pushing:", m.toString());
                observable.addNewMensa(m);
                servedRoutes.add(apiRoute.id);
                Log.d("size:" , "served " + servedRoutes.size());
                Log.d("size:" , "routes " + ROUTES.length);

                // TODO lock
            //    if(servedRoutes.size() == ROUTES.length) {
                //    observable.notifyAllObservers();
               //     servedRoutes.clear();
              //  }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.e("fail", error.getMessage());
            Log.e("id:", apiRoute.id+ "");
            servedRoutes.add(apiRoute.id);
            if(servedRoutes.size() == ROUTES.length) {
                observable.notifyAllObservers();
                servedRoutes.clear();
            }

        }



        private Mensa parseXML(String name, byte[] xmlFile, Mensa.Weekday day, Mensa.MenuCategory mealType) throws  IOException, SAXException, ParserConfigurationException {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(xmlFile));
            Element element=doc.getDocumentElement();
            element.normalize();
            NodeList nList = doc.getElementsByTagName("summary");
            Node summaryNode = nList.item(0);

            Node divNode = null;

//            Log.d("sum node", summaryNode.getNodeName());


  //          Log.d("length", summaryNode.getChildNodes().getLength() + "");
            for (int i = 0; i < summaryNode.getChildNodes().getLength(); i++) {
                Node n = summaryNode.getChildNodes().item(i);
                if(n.getNodeName().equals("div")) {
                    Mensa m = new Mensa(name, name);
                    m.addMenuForDayAndCategory(day,mealType,getMensaFromDivNode(n));
                    return m;
                }
         //       Log.d("n", n.getNodeName());
            }

            return new Mensa("Error no div node found", "ERROR");

        }


        private String domAsString(Node rootnode) {
            String retString = trimString(rootnode.getNodeValue()) + (rootnode.getNodeName().equals("#text") ? " " : "\n");
            NodeList children = rootnode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                retString  += domAsString(children.item(i));
            }

            return retString;
        }
        private String trimString(String stringToTrim) {
            if(stringToTrim == null) {
                return "";
            }
            return stringToTrim.trim();
        }

        private List<IMenu> getMensaFromDivNode(Node divNode) {
            List<IMenu> menuList = new ArrayList<>();

            Menu currentMenu = null;
            for (int i = 0; i < divNode.getChildNodes().getLength(); i++) {
                Node n = divNode.getChildNodes().item(i);

                if(n.getNodeName().equals("h3")) {
                    currentMenu = new Menu(null,null,null,null);
                    menuList.add(currentMenu);

                    // found new Meal
                    NodeList children = n.getChildNodes();
                    currentMenu.setName(trimString(children.item(0).getNodeValue()));
                    currentMenu.setPrices(trimString(children.item(1).getFirstChild().getNodeValue()));
                }

                if(n.getNodeName().equals("p")) {

                    currentMenu.setDescription(domAsString(n));
         //           Log.d("string, " , currentMenu.toString());
                }
           //     Log.d("n", n.getNodeName());
            }
            return menuList;
        }

    }
}
