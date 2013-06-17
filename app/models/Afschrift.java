package models;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

@Entity
public class Afschrift extends Model {

    public enum AfBij { AF, BIJ };
    
    @GeneratedValue(strategy=GenerationType.AUTO, generator="afschrift_seq_gen")
    @SequenceGenerator(name="afschrift_seq_gen", sequenceName="AFSCHRIFT_SEQ")
    @Id
    public Long id;
    
    @Constraints.Required
    public Date datum;

    @Constraints.Required
    public BigDecimal bedrag;
    
    @Constraints.Required
    public AfBij afbij;
    
    @Constraints.Required
    public String tegenrekening;
    
    public String naam;
    public String mededelingen;

    public Afschrift(Date datum, String naam, BigDecimal bedrag, AfBij afbij, String tegenrekening, String mededelingen) {
        this.datum = datum;
        this.naam = naam;
        this.bedrag = bedrag;
        this.afbij = afbij;
        this.tegenrekening = tegenrekening;
        this.mededelingen = mededelingen;
    }

    public static void create(Afschrift afschrift) {
        afschrift.save();
    }


    public static Finder<Long,Afschrift> find = new Finder<Long, Afschrift>(
            Long.class, Afschrift.class
          );
}
