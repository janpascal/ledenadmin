package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

@Entity
public class Persoon extends Model {

    @GeneratedValue(strategy=GenerationType.AUTO, generator="persoon_seq_gen")
    @SequenceGenerator(name="persoon_seq_gen", sequenceName="PERSOON_SEQ")
    @Id
    public Long id;
    
    @Constraints.Required
    public String name;
    
    public String email;
    

    public Persoon(String name) {
        this.name = name;
    }

    public Persoon(String name, String email) {
        this.name = name;
        this.email = email;
    }
    public static void create(Persoon p) {
        p.save();
    }

    public static void delete(Long id) {
       find.ref(id).delete();
    }
     
    public static Finder<Long,Persoon> find = new Finder<Long, Persoon>(
            Long.class, Persoon.class
          );
}
