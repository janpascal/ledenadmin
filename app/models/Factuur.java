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
    @Formats.DateTime(pattern="dd/MM/yyyy")
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
   
    public static Page<Factuur> page(int page, int pageSize, String sortBy, String order, String filter, int jaarFilter, String betaaldFilter) {
      /*
        Date jan1 = new GregorianCalendar(jaarFilter,0,1,0,0).getTime();
        if(jaarFilter==0) jaarFilter=Integer.MAX_VALUE;
        Date dec31 = new GregorianCalendar(jaarFilter+1,0,1,0,0).getTime();
        */
        ExpressionList<Factuur> e = 
            find.where()
                .ilike("lid.personen.name", "%" + filter + "%");
        if(jaarFilter>=0) {
            e = e.eq("jaar", jaarFilter);
        }
        if("ja".equals(betaaldFilter)) {
            e = e.isNotNull("betaling");
        } else if("nee".equals(betaaldFilter)) {
            e = e.isNull("betaling");
        }

        return e 
                .order(sortBy + " " + order)
                .fetch("lid.personen", new FetchConfig().query())
                //.orderBy("lid.personen.name"+ " " + order)
                .findPagingList(pageSize)
                .getPage(page);
    }
                //.fetch("lid.personen")
                //.orderBy(sortBy + " " + order)
                //.orderBy("lid.personen.name" + " " + order)
 
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
                    .eq("afbij", Afschrift.AfBij.BIJ)
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
