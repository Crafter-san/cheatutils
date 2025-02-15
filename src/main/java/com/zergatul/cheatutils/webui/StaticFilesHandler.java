package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFilesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String filename;
        if (exchange.getRequestURI().getPath().equals("/")) {
            filename = "/index.html";
        } else {
            filename = exchange.getRequestURI().getPath();
        }

        byte[] bytes;
        InputStream stream = FMLEnvironment.production ?
                loadFromResource("web" + filename) :
                loadFromFile("web" + filename);
        try (stream) {
            if (stream == null) {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
                return;
            }

            bytes = org.apache.commons.io.IOUtils.toByteArray(stream);
        }
        catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(503, 0);
            exchange.close();
            return;
        }

        HttpHelper.setContentType(exchange, filename);

        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
        exchange.close();
    }

    private static InputStream loadFromResource(String filename) {
        ClassLoader classLoader = StaticFilesHandler.class.getClassLoader();
        return classLoader.getResourceAsStream(filename);
    }

    private static InputStream loadFromFile(String filename) {
        try {
            Path path = Paths.get(System.getProperty("user.dir"), "../src/main/resources", filename);
            return new FileInputStream(path.toString());
        }
        catch (IOException e) {
            return null;
        }
    }
}