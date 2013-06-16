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
    
    @OneToMany(cascade=CascadeType.ALL)
    public List<Persoon> personen;
    
    @Constraints.Required
    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date lidSinds;
    
    public Date lidTot;
    
    public String address;
    
    public List<String> rekeningnummers;

    public Lid(String name, Date lidSinds) {
        this.personen = new ArrayList<Persoon>();
        this.personen.add(new Persoon(name));
        this.rekeningnummers = new ArrayList<String>();
        this.lidSinds = lidSinds;
    }

    public Lid(String name, String address, Date lidSinds) {
        this.personen = new ArrayList<Persoon>();
        this.personen.add(new Persoon(name));
        this.address = address;
        this.lidSinds = lidSinds;
        this.rekeningnummers = new ArrayList<String>();
    }

    public Lid(Long id, String name1, String name2, String address, Date lidSinds) {
        this.id = id;
        this.personen = new ArrayList<Persoon>();
        this.personen.add(new Persoon(name1));
        this.personen.add(new Persoon(name2));
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
     
    public static Finder<Long,Lid> find = new Finder<Long, Lid>(
            Long.class, Lid.class
          );
}
