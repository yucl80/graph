package com.yucl.learn.graph;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileGenerate {
    public static void main(String[] args) {

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bytes = Files.readAllBytes(Paths.get("test.puml"));
            SourceStringReader reader = new SourceStringReader(new String(bytes,StandardCharsets.UTF_8));
            reader.outputImage(byteArrayOutputStream, new FileFormatOption(FileFormat.SVG));

            String svg = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
            svg = svg.replaceAll("(?s)<!--.*?-->", "");
            FileOutputStream fileOutputStream = new FileOutputStream(String.format("%s/%s.svg", "d://", "ax"));
            fileOutputStream.write(svg.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
