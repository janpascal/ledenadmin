package models;

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
    public Long bedrag;
    
    @Constraints.Required
    public AfBij afbij;
    
    @Constraints.Required
    public String tegenrekening;
    
    public String mededelingen;

    public Afschrift(Long bedrag, AfBij afbij, String tegenrekening, String mededelingen) {
        this.bedrag = bedrag;
        this.afbij = afbij;
        this.tegenrekening = tegenrekening;
        this.mededelingen = mededelingen;
    }
}
