package com.yucl.learn.graph;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import net.sourceforge.plantuml.json.JsonArray;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        toDotSub();

    }

    static void changePos() throws IOException {
        String data = new String(Files.readAllBytes(Paths.get("les-miserables.json")));
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONArray nodes = jsonObject.getJSONArray("nodes");
        Map<String,Pos> map = f();
        for(int i=0;i< nodes.size();i++){
            JSONObject node = nodes.getJSONObject(i);
            String id = node.getString("id");
            Pos pos = map.get(id);
            node.put("x",Double.parseDouble(pos.cx));
            node.put("y",Double.parseDouble(pos.cy));
        }
        System.out.println(jsonObject.toJSONString());

    }

    static String toDotSub() throws IOException {
        String data = new String(Files.readAllBytes(Paths.get("les-miserables.json")));
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONArray nodes = jsonObject.getJSONArray("nodes");
        JSONArray links = jsonObject.getJSONArray("links");
        Map<Integer, List<JSONObject>> map  = new HashMap<>();
        for(int i=0;i< nodes.size();i++){
            JSONObject node = nodes.getJSONObject(i);
            Integer category = node.getInteger("category");
            map.computeIfAbsent(category,k->new JSONArray()).add(node);

        }
        map.forEach((k,v)->{
            sb.append(" subgraph cluster_"+k + "{\n");
            for(int i=0;i< v.size();i++){
                JSONObject node = nodes.getJSONObject(i);
                String id = node.getString("id");
                String label= node.getString("name");
                sb.append( id + " [ id="+id+",  label=<"+label+ ">, style=\"rounded, solid\" color=\"#b81b1b\" fillcolor=\"#b02222\" shape=circle]\n");
            }
            sb.append("}\n");
        });

        for(int i=0;i< links.size();i++){
            JSONObject link = links.getJSONObject(i);
            String src = link.getString("source");
            String target= link.getString("target");
            sb.append( src + " -> " +target + "\n");
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    static String toDot() throws IOException {
        String data = new String(Files.readAllBytes(Paths.get("les-miserables.json")));
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONArray nodes = jsonObject.getJSONArray("nodes");
        JSONArray links = jsonObject.getJSONArray("links");
        for(int i=0;i< nodes.size();i++){
            JSONObject node = nodes.getJSONObject(i);
            String id = node.getString("id");
            String label= node.getString("name");
            sb.append( id + " [ id="+id+",  label=<"+label+ ">, style=\"rounded, solid\" color=\"#b81b1b\" fillcolor=\"#b02222\" shape=circle]\n");
        }
        for(int i=0;i< links.size();i++){
            JSONObject link = links.getJSONObject(i);
            String src = link.getString("source");
            String target= link.getString("target");
            sb.append( src + " -> " +target + "\n");
        }
        return sb.toString();
    }

    static Map<String,Pos> f() {
        Map<String,Pos> map = new HashMap<>();
        try {
            File file = new File("test.svg");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("g");
            System.out.println(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NamedNodeMap attrs = node.getAttributes();
                if(attrs.getNamedItem("class").getNodeValue().equals("node")) {
                    String id =attrs.getNamedItem("id").getNodeValue();
                    NodeList nodes = node.getChildNodes();
                    for (int j=0;j<nodes.getLength();j++) {
                        Node n =  nodes.item(j);
                       // System.out.println(n.getNodeName());
                        if(n.getNodeName() .equals("ellipse")){
                            NamedNodeMap as = n.getAttributes();
                            System.out.println(as.getNamedItem("cx").getNodeValue());
                            map.put(id,new Pos(as.getNamedItem("cx").getNodeValue(),as.getNamedItem("cy").getNodeValue()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return map;
    }
    static class Pos {
        public String cx;
        public String cy;

        public Pos(String cx, String cy) {
            this.cx = cx;
            this.cy = cy;
        }
    }
}
