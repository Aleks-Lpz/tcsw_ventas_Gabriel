package com.ventas.adapter.in.rest.dto;

/**
 * Cuerpo de error (esquema ProblemaDetalle de docs/openapi.yaml), usado en las respuestas 400 y 409.
 */
public class ProblemaDetalle {

    private final String type;
    private final String title;
    private final int status;
    private final String detail;

    public ProblemaDetalle(String type, String title, int status, String detail) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }
}
