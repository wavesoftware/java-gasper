package pl.wavesoftware.examples.sparktester;

import static spark.Spark.*;

public class HelloApp {
    public static void main(String[] args) {
        get("/", (req, res) -> "Hello from Spark!");
    }
}
