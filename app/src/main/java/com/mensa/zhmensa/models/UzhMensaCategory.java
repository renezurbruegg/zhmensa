package com.mensa.zhmensa.models;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mensa.zhmensa.services.HttpUtils;

import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cz.msebera.android.httpclient.Header;

public class UzhMensaCategory extends MensaCategory {

    private static final MensaApiRoute[] ROUTES = {
            new MensaApiRoute(147, "UZH untere Mensa A"),
            new MensaApiRoute(148, "UZH obere Mensa B"),
            new MensaApiRoute(150, "UZH Lichthof"),
            new MensaApiRoute(180, "UZH Irchel"),
            new MensaApiRoute(146, "ZH Tierspital"),
            new MensaApiRoute(151, "UZH Zentrum Für Zahnmedizin"),
            new MensaApiRoute(143, "ZH Platte"),
            new MensaApiRoute(346, "UZH Rämi 59 (vegan)"),
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
     */
    private final List<Integer> servedRoutes = new ArrayList<>();

    public UzhMensaCategory(String displayName) {
        super(displayName);
    }


    @Override
    public Observable loadMensasFromAPI() {
        final MensaListObservable obs = new MensaListObservable();
        for(MensaApiRoute route : ROUTES) {
            HttpUtils.getByUrl("http://zfv.ch/de/menus/rssMenuPlan?menuId="+route.id +"&&dayOfWeek=1", new RequestParams(), new XMLResponseHandler(obs, route));
        }
        return obs;
    }



    private static class MensaApiRoute {
        public int id;
        public String name;

        public MensaApiRoute(int id, String name) {
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
            Log.e("reposne", new String(responseBody));
            try {
                Mensa m = parseXML(responseBody);
                m.setDisplayName(apiRoute.name);
                observable.pushSilently(m);
                servedRoutes.add(apiRoute.id);
                // TODO lock
                if(servedRoutes.size() == ROUTES.length) {
                    observable.notifyAllObservers();
                    servedRoutes.clear();
                }

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

        }



        private Mensa parseXML(byte[] xmlFile) throws  IOException, SAXException, ParserConfigurationException {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(xmlFile));
            Element element=doc.getDocumentElement();
            element.normalize();
            NodeList nList = doc.getElementsByTagName("summary");
            Node summaryNode = nList.item(0);

            Node divNode = null;

            Log.d("sum node", summaryNode.getNodeName());


            Log.d("length", summaryNode.getChildNodes().getLength() + "");
            for (int i = 0; i < summaryNode.getChildNodes().getLength(); i++) {
                Node n = summaryNode.getChildNodes().item(i);
                if(n.getNodeName().equals("div")) {
                    return new Mensa("", getMensaFromDivNode(n));
                }
                Log.d("n", n.getNodeName());
            }

            return new Mensa("Error no div node found");

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
                    Log.d("string, " , currentMenu.toString());
                }
                Log.d("n", n.getNodeName());
            }
            return menuList;
        }

    }
}
