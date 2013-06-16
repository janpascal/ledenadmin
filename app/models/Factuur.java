package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

@Entity
public class Factuur extends Model {

    @GeneratedValue(strategy=GenerationType.AUTO, generator="factuur_seq_gen")
    @SequenceGenerator(name="factuur_seq_gen", sequenceName="FACTUUR_SEQ")
    @Id
    public Long id;
    
    @Constraints.Required
    public Long bedrag;
    
    public Afschrift betaling;

    public Factuur(Long bedrag) {
        this.bedrag = bedrag;
    }
    
    public boolean isBetaald() {
        return betaling != null;
    }
    
    public static Finder<Long,Factuur> find = new Finder<Long, Factuur>(
            Long.class, Factuur.class
    );
}
