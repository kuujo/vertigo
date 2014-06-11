import net.kuujo.vertigo.java.ComponentVerticle;

public class App extends ComponentVerticle {

  @Override
  public void start() {
    output.port("out").send("Hello world!");
  }

}
