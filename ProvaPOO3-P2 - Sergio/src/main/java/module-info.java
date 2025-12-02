module elieldm.provapoo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires com.zaxxer.hikari;
    requires org.slf4j;
    requires static lombok;

    exports com.example.provapoo3;

    opens com.example.provapoo3 to javafx.fxml;
    opens com.example.provapoo3.controller to javafx.fxml;
    opens com.example.provapoo3.model to javafx.base, org.hibernate.orm.core;
    opens com.example.provapoo3.view to javafx.fxml;
    opens com.example.provapoo3.dao to org.hibernate.orm.core;
    opens com.example.provapoo3.utils to org.hibernate.orm.core;


}
