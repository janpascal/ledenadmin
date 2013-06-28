package models;

import java.util.*;
import java.math.BigDecimal;
import javax.persistence.*;

import play.Logger;
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
    
    @ManyToOne
    public Lid lid;
    
    @ManyToOne
    public Afschrift betaling;

    public Factuur(Date datum, Lid lid, BigDecimal bedrag) {
        this.datum = datum;
        this.lid = lid;
        this.bedrag = bedrag;
    }

    public String betrokkene() {
      if(lid != null) {
          return lid.getFirstName();
      } else {
        return "";
      }
    }

   
    public static Page<Factuur> page(int page, int pageSize, String sortBy, String order, String filter, int jaarFilter, String betaaldFilter) {
      /*
        Date jan1 = new GregorianCalendar(jaarFilter,0,1,0,0).getTime();
        if(jaarFilter==0) jaarFilter=Integer.MAX_VALUE;
        Date dec31 = new GregorianCalendar(jaarFilter+1,0,1,0,0).getTime();
        */
        /*
        String sql = " select order_id, o.status, c.id, c.name, sum(d.order_qty*d.unit_price) as totalAmount"
                + " from o_order o" 
                + " join o_customer c on c.id = o.kcustomer_id "
                + " join o_order_detail d on d.order_id = o.id " 
                + " group by order_id, o.status ";
       
        RawSql rawSql = 
        RawSqlBuilder.parse(sql)
            // map the sql result columns to bean properties
            .columnMapping("order_id", "order.id")
            .columnMapping("o.status", "order.status")
            .columnMapping("c.id", "order.customer.id")
            .columnMapping("c.name","order.customer.name")
            // we don't need to map this one due to the sql column alias
            //.columnMapping("sum(d.order_qty*d.unit_price)", "totalAmount")
            .create();
            */
            /*
        String sql = "select distinct factuur.id, factuur._type, factuur.bedrag, "
            + " factuur.datum, factuur.lid_id, factuur.betaling_id, factuur.naam,"
            + " factuur.omschrijving, factuur.jaar "
            + " from factuur "
            + " left join lid on lid.id = factuur.lid_id "
            + " left join persoon on persoon.lid_id = lid.id";
        RawSql rawSql = RawSqlBuilder.parse(sql)
            // map the sql result columns to bean properties
            .columnMapping("factuur.id", "id")
            .columnMapping("factuur._type", "_type")
            .columnMapping("factuur.bedrag", "bedrag")
            .columnMapping("factuur.datum", "datum")
            .columnMapping("factuur.lid_id", "lid.id")
            .columnMapping("factuur.betaling_id", "betaling.id")            
            .columnMapping("factuur.naam", "naam")
            .columnMapping("factuur.omschrijving", "omschrijving")
            .columnMapping("factuur.jaar", "jaar")
            //.columnMapping("o.status", "order.status")
            //.columnMapping("c.id", "order.customer.id")
            //.columnMapping("c.name","order.customer.name")
            // we don't need to map this one due to the sql column alias
            //.columnMapping("sum(d.order_qty*d.unit_price)", "totalAmount")
            .create();
*/
/*
        String sql = "select distinct factuur.id "
            + " from factuur "
            + " left join lid on lid.id = factuur.lid_id "
            + " left join persoon on persoon.lid_id = lid.id";
        RawSql rawSql = RawSqlBuilder.parse(sql)
            // map the sql result columns to bean properties
            .columnMapping("factuur.id", "id")
            .create();
        com.avaje.ebean.Query<Factuur> query = Ebean.find(Factuur.class);
        ExpressionList<Factuur> e = query.setRawSql(rawSql)
            .select("id")
            .where()
            .or(Expr.ilike("persoon.name", "%" + filter + "%"),
                Expr.ilike("naam", "%"+filter+"%"));
        if(jaarFilter>=0) {
            e = e.eq("jaar", jaarFilter);
        }
        if("ja".equals(betaaldFilter)) {
            e = e.isNotNull("betaling");
        } else if("nee".equals(betaaldFilter)) {
            e = e.isNull("betaling");
        }
*/

        // FIXME: this uses a JOIN instead of a LEFT JOIN
        // which makes it only find objects for which 
        // lid.personen.name is set
        ExpressionList<Factuur> e = 
            find.where();

        if (filter!=null && !"".equals(filter)) {
          e = e.or(Expr.ilike("lid.personen.name", "%" + filter + "%"),
                    Expr.ilike("naam", "%"+filter+"%"));
        }

        if(jaarFilter>=0) {
            e = e.eq("jaar", jaarFilter);
        }
        if("ja".equals(betaaldFilter)) {
            e = e.isNotNull("betaling");
        } else if("nee".equals(betaaldFilter)) {
            e = e.isNull("betaling");
        }

        com.avaje.ebean.Query<Factuur> q = e
                .order(sortBy + " " + order)
                .fetch("lid.personen", new FetchConfig().query());
                //.orderBy("lid.personen.name"+ " " + order)
        Page<Factuur> result = q
                .findPagingList(pageSize)
                .getPage(page);

        //Logger.info("Used SQL: "+query.getGeneratedSql());
        //q.findList();
        //Logger.info("Used SQL: "+q.getGeneratedSql());

        return result;
    }
                //.fetch("lid.personen")
                //.orderBy(sortBy + " " + order)
                //.orderBy("lid.personen.name" + " " + order)
 
    public boolean isBetaald() {
        return betaling != null;
    }
    
    public List<Afschrift> possiblePayments() {
        if(lid==null || lid.bankrekeningen.isEmpty()) {
          if (betaling != null) {
            return Collections.singletonList(betaling);
          }
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
        List<Afschrift> result = junction
               .orderBy("datum asc")
               .findList(); 
        if (betaling != null && !result.contains(betaling)) {
          result.add(betaling);
        }
        return result;
    }

    public void markeerBetaling(Afschrift afschrift) {
      this.betaling = afschrift;
      afschrift.betaaldeFacturen.add(this);
    }

    public static void delete(Long id) {
       find.ref(id).delete();
    }
     
    public static Finder<Long,Factuur> find = new Finder<Long, Factuur>(
            Long.class, Factuur.class
    );
}
