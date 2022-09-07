package com.yucl.learn.graph;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

import java.io.File;
import java.io.InputStream;

public class TestNidiGraphviz {

    public static void main( String[] args )
    {
        try (InputStream dot = App.class.getResourceAsStream("/color.dot")) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("example/ex4-1.png"));

            g.graphAttrs()
                    .add(Color.WHITE.gradient(Color.rgb("888888")).background().angle(90))
                    .nodeAttrs().add(Color.WHITE.fill())
                    .nodes().forEach(node ->
                            node.add(
                                    Color.named(node.name().toString()),
                                    Style.lineWidth(4), Style.FILLED));
            Graphviz.fromGraph(g).width(700).render(Format.DOT).toFile(new File("example/ex4-2.png"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
