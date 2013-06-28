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
@DiscriminatorValue("1")
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

    // genereer contributiefacturen voor een bepaald jaar. Alleen voor de 
    // leden die in dat jaar lid waren
    public static List<Factuur> generateInvoices(int year, BigDecimal amount) {
      Date jan1 = new GregorianCalendar(year,0,1,0,0).getTime();
      Date dec31 = new GregorianCalendar(year+1,0,1,0,0).getTime();
      // Find eligible members
      List<Lid> leden = Lid.find.where()
         .or(Expr.isNull("lidSinds"), Expr.lt("lidSinds", dec31))
         .or(Expr.isNull("lidTot"), Expr.gt("lidTot", jan1))
         .findList();
      List<Factuur> result = new ArrayList<Factuur>(leden.size());
      Date now = new Date();
      for(Lid lid: leden) {
        result.add(new FactuurContributie(now, lid, amount, year));
      }
      return result;
    }

   public static List<Integer> jarenMetContributieFacturen() {
      List<Integer> jaren = new ArrayList<Integer>();
      String sql = "select distinct jaar from factuur where jaar is not null order by jaar asc";
      SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
      List<SqlRow> list = sqlQuery.findList();
      for (SqlRow row: list) {
        jaren.add(row.getInteger("jaar"));
      }
      return jaren;
   }

    public static Finder<Long,FactuurContributie> find = new Finder<Long, FactuurContributie>(
            Long.class, FactuurContributie.class
    );

    public String toString() {
      return "Contributie "+jaar;
    }
}
