package models;

import java.util.*;
import java.math.BigDecimal;
import javax.persistence.*;

import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

@Entity
@Inheritance
@DiscriminatorColumn(name = "_type", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
public class Factuur extends Model {

    @GeneratedValue(strategy=GenerationType.AUTO, generator="factuur_seq_gen")
    @SequenceGenerator(name="factuur_seq_gen", sequenceName="FACTUUR_SEQ")
    @Id
    public Long id;
    
    @Constraints.Required
    public BigDecimal bedrag;
    
    @Constraints.Required
    public Date datum;
    
    @Constraints.Required
    @ManyToOne
    public Lid lid;
    
    @ManyToOne
    public Afschrift betaling;

    public Factuur(Date datum, Lid lid, BigDecimal bedrag) {
        this.datum = datum;
        this.lid = lid;
        this.bedrag = bedrag;
    }
    
    public boolean isBetaald() {
        return betaling != null;
    }
    
    public List<Afschrift> possiblePayments() {
        if(lid.bankrekeningen.isEmpty()) {
          return new ArrayList<Afschrift>();
        }
        // tegenrekening in bekende bankrekeningen lid
        // misschien: datum overboeking na datum factuur
        Junction<Afschrift> junction = 
                Afschrift.find
                  .where()
                    .disjunction();
        for(Bankrekening rek: lid.bankrekeningen) {
          junction.add(Expr.eq("tegenrekening", rek.nummer));
        }
        List<Afschrift> result = junction.findList(); 
        return result;
    }

    public static Finder<Long,Factuur> find = new Finder<Long, Factuur>(
            Long.class, Factuur.class
    );
}
