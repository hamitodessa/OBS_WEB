// Updated Entity Files for PostgreSQL Compatibility

package com.hamit.obs.model.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "etiket_ayar")
@Data
@NoArgsConstructor
public class Etiket_Ayarlari {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "etiket_ayar_seq")
    @SequenceGenerator(name = "etiket_ayar_seq", sequenceName = "etiket_ayar_sequence", allocationSize = 1)
    private Long id;

    @Column
    private Integer altbosluk;

    @Column
    private Integer ustbosluk;

    @Column
    private Integer solbosluk;

    @Column
    private Integer sagbosluk;

    @Column
    private Integer yukseklik;

    @Column
    private Integer genislik;

    @Column
    private Integer yataydikey;

    @Column
    private Integer dikeyarabosluk;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
