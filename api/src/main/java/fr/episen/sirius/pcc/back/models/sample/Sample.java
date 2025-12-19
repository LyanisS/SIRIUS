package fr.episen.sirius.pcc.back.models.sample;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "sample")
public class Sample {

    @Id
    @Column(name="id_sample")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSample;

    @Column(name = "date_sample")
    private Date dateSample;

    @Column(name = "string_sample")
    private String stringSample;

    @Column(name = "float_sample")
    private Float floatSample;
}
