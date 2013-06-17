package models;

import java.math.BigDecimal;
import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

@Entity
public class FactuurContributie extends Factuur {

    public int jaar;

    public FactuurContributie(Date datum, Lid lid, BigDecimal bedrag, int jaar) {
        super(datum, lid, bedrag);
        this.jaar = jaar;
    }
    
    public FactuurContributie(Date datum, Lid lid, int bedrag, int jaar) {
        super(datum, lid, new BigDecimal(bedrag));
        this.jaar = jaar;
    }

    public List<Afschrift> possiblePayments() {
        if(lid.rekeningnummers.isEmpty()) {
          return new ArrayList<Afschrift>();
        }
        // tegenrekening in bekende rekeningnummers lid
        // misschien: datum overboeking na datum factuur
        Junction<Afschrift> junction = 
                Afschrift.find
                  .where()
                    .conjunction();
        for(Bankrekening nr: lid.rekeningnummers) {
          junction.add(Expr.eq("tegenrekening", nr.rekeningnummer));
        }
        List<Afschrift> result = junction.findList(); 
        return result;
    }
    
    public static Finder<Long,FactuurContributie> find = new Finder<Long, FactuurContributie>(
            Long.class, FactuurContributie.class
    );

}
