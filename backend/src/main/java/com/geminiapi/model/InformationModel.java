package com.geminiapi.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat")
public class InformationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    @Column(name = "perguntauser")
    private String perguntaUser;  // Coluna para armazenar a pergunta do usuário
    @Column(name = "respostaia")
    private String respostaIa;    // Coluna para armazenar a resposta da IA

    @Column(nullable = false, updatable = false)
    private LocalDateTime data;  // Coluna para armazenar a data/hora que a informação foi salva
    @PrePersist
    protected void onCreate() {
        data = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPerguntaUser() {
        return perguntaUser;
    }

    public void setPerguntaUser(String perguntaUser) {
        this.perguntaUser = perguntaUser;
    }

    public String getRespostaIa() {
        return respostaIa;
    }

    public void setRespostaIa(String respostaIa) {
        this.respostaIa = respostaIa;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }
}

