package com.delphi.delphi.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_history")
public class UserChatHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "chatHistory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserChatMessage> messages;

    @ManyToOne
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    public UserChatHistory() {
    }

    public UserChatHistory(List<UserChatMessage> messages, Assessment assessment) {
        this.messages = messages;
        this.assessment = assessment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<UserChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<UserChatMessage> messages) {
        this.messages = messages;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }
}
