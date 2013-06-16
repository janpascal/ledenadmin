package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

@Entity
public class Betaling extends Model {

    public enum DebetCredit { DEBET, CREDIT };
    
    @GeneratedValue(strategy=GenerationType.AUTO, generator="betaling_seq_gen")
    @SequenceGenerator(name="betaling_seq_gen", sequenceName="BETALING_SEQ")
    @Id
    public Long id;
    
    @Constraints.Required
    public Long bedrag;
    
    @Constraints.Required
    public DebetCredit dc;
    
    @Constraints.Required
    public String tegenrekening;
    
    public String vermelding;

    public Betaling(Long bedrag, DebetCredit dc, String tegenrekening, String vermelding) {
        this.bedrag = bedrag;
        this.dc = dc;
        this.tegenrekening = tegenrekening;
        this.vermelding = vermelding;
    }
}
