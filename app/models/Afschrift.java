package models;

import java.util.*;
import java.math.BigDecimal;
import javax.persistence.*;

import play.Logger;
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

    @OneToMany(mappedBy="betaling")
    public List<Factuur> betaaldeFacturen;

    public Afschrift(Date datum, String naam, BigDecimal bedrag, AfBij afbij, String tegenrekening, String mededelingen) {
        this.datum = datum;
        this.naam = naam;
        this.bedrag = bedrag;
        this.afbij = afbij;
        this.tegenrekening = tegenrekening;
        this.mededelingen = mededelingen;
        this.betaaldeFacturen = new ArrayList<Factuur>();
    }

    public static Page<Afschrift> page(int page, int pageSize, String sortBy, String order, String filter, int jaarFilter, String verantwoordFilter) {
        ExpressionList<Afschrift> e = 
            find.where()
                .or(Expr.ilike("naam", "%" + filter + "%"),
                    Expr.ilike("mededelingen", "%" + filter + "%"));
        if(jaarFilter>=0) {
            Date jan1 = new GregorianCalendar(jaarFilter,0,1,0,0).getTime();
            if(jaarFilter==0) jaarFilter=Integer.MAX_VALUE;
            Date dec31 = new GregorianCalendar(jaarFilter+1,0,1,0,0).getTime();

            e = e.between("datum", jan1, dec31);
        }
        // Filter in the template, not here
        if (!verantwoordFilter.isEmpty()) pageSize = 0;
        /*
        if (!verantwoordFilter.isEmpty()) {
            Query<Factuur> subQuery =   
                Ebean.createQuery(Factuur.class)  
                    .select("sku")  
                    .where().idEq(4).query();  
              
            List<MinCustomer> list = Ebean.find(MinCustomer.class)  
                .where().in("name", subQuery)  
                .findList();  
       }*/
        /*
        FIXME
        if("ja".equals(verantwoordFilter)) {
            e = e.isNotNull("betaling");
        } else if("nee".equals(betaaldFilter)) {
            e = e.isNull("betaling");
        }
*/
        return e 
                .order(sortBy + " " + order)
                //.fetch("lid.personen", new FetchConfig().query())
                //.orderBy("lid.personen.name"+ " " + order)
                .findPagingList(pageSize)
                .getPage(page);
    }
 
    // Afschrift gedekt door facturen? 
    public boolean isVerantwoord() {
      BigDecimal som = new BigDecimal(0);
      for(Factuur f: betaaldeFacturen) {
        som = som.add(f.bedrag);
      }
      return som.equals(bedrag);
    }

    public static void create(Afschrift afschrift) {
        afschrift.save();
    }

    public String toString() {
      return "Afschrift "+datum.toString()+" "+naam+" "+bedrag;
    }

    public static Finder<Long,Afschrift> find = new Finder<Long, Afschrift>(
            Long.class, Afschrift.class
          );
}
