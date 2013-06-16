package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

@Entity
public class Lid extends Model {

    @GeneratedValue(strategy=GenerationType.AUTO, generator="lid_seq_gen")
    @SequenceGenerator(name="lid_seq_gen", sequenceName="LID_SEQ")
    @Id
    public Long id;
    
    @Constraints.Required
    public String name;
    
    @Constraints.Required
    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date lidSinds;
    
    public Date lidTot;
    
    public String address;
    
    public List<String> rekeningnummers;

    public Lid(String name, Date lidSinds) {
        this.name = name;
        this.rekeningnummers = new ArrayList<String>();
        this.lidSinds = lidSinds;
    }

    public Lid(String name, String address, Date lidSinds) {
        this.name = name;
        this.address = address;
        this.lidSinds = lidSinds;
        this.rekeningnummers = new ArrayList<String>();
    }
    
    public static void create(Lid lid) {
        lid.save();
    }

    public static void delete(Long id) {
       find.ref(id).delete();
    }
     
    public static Finder<Long,Lid> find = new Finder(
            Long.class, Lid.class
          );
}
