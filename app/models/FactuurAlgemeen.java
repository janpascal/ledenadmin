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
@DiscriminatorValue("2")
public class FactuurAlgemeen extends Factuur {

    public String naam;
    public String omschrijving;

    public FactuurAlgemeen(Date datum, Lid lid, BigDecimal bedrag, String naam, String omschrijving) {
        super(datum, lid, bedrag);
        this.naam = naam;
        this.omschrijving = omschrijving;
    }

    public static Finder<Long,FactuurAlgemeen> find = new Finder<Long, FactuurAlgemeen>(
            Long.class, FactuurAlgemeen.class
    );

    public String toString() {
      return naam + " (" + omschrijving + ")";
    }

    @Override
    public String betrokkene() {
      return naam;
    }
}
