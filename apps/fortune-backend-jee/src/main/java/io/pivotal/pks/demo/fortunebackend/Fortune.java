package io.pivotal.pks.demo.fortunebackend;


/**
 * Created by azwickey on 1/12/18.
 */
public class Fortune {

    private Long id;
    private String text;

    public Fortune() {  }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return getId() + ", " + getText();
    }
}
