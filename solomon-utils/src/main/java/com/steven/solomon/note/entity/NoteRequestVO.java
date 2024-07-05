package com.steven.solomon.note.entity;

import java.io.Serializable;

public class NoteRequestVO implements Serializable {

    private boolean ok;

    private String body;

    public NoteRequestVO(){
        super();
    }

    public NoteRequestVO(boolean ok,String body){
        super();
        this.ok = ok;
        this.body = body;
    }

    public boolean getOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
