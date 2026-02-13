package com.hamit.obs.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "user_shortcut",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","action_code"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User_Shortcut {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_shortcut_seq")
    @SequenceGenerator(name = "user_shortcut_seq", sequenceName = "user_shortcut_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "action_code", nullable = false, length = 30)
    private String actionCode;

    @Column(name = "hotkey", nullable = false, length = 5)
    private String hotkey;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

