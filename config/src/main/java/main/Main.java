package main;

import io.vertx.core.Vertx;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.network.Network;

public class Main {

  public static void main(String[] args) {
    Vertigo v = Vertigo.vertigo(Vertx.vertx());
    
    NetworkBuilder netB = Network.builder("MyNet");
    
    netB.component("Producer", Producer.class.getCanonicalName());
    netB.component("Consumer", Consumer.class.getCanonicalName());
    
    netB.connect("Producer").port("out").to("Consumer").port("in");
    
    v.deployNetwork(netB.build(), ar -> {
      System.out.println(ar.succeeded() ? ar.result() : ar.cause());
    });

    Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080);
  }

}
