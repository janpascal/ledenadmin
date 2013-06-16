package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

@Entity
public class FactuurContributie extends Factuur {

    public int jaar;

    public FactuurContributie(Long bedrag, int jaar) {
        super(bedrag);
        this.jaar = jaar;
    }
}
