open module com.guicedee.runtime.autoconfigure.test {
    requires transitive com.guicedee.runtime.autoconfigure;
    requires com.guicedee.guicedinjection;
    requires com.google.guice;
    requires java.net.http;

    requires org.junit.jupiter;
}
