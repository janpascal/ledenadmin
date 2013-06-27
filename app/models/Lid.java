package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;
import com.avaje.ebean.annotation.PrivateOwned;

@Entity
public class Lid extends Model {

    @GeneratedValue(strategy=GenerationType.AUTO, generator="lid_seq_gen")
    @SequenceGenerator(name="lid_seq_gen", sequenceName="LID_SEQ")
    @Id
    public Long id;
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="lid", orphanRemoval = true)
    @PrivateOwned
    @javax.persistence.OrderBy("id ASC")
    public List<Persoon> personen;
    
    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date lidSinds;
    
    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date lidTot;
   
    public String address;
   
    @OneToMany(cascade=CascadeType.ALL, mappedBy="lid", orphanRemoval = true)
    @PrivateOwned
    @javax.persistence.OrderBy("id ASC")
    public List<Bankrekening> bankrekeningen;

    public Lid(String name, Date lidSinds) {
        this.personen = new ArrayList<Persoon>();
        this.personen.add(new Persoon(this, name));
        this.bankrekeningen = new ArrayList<Bankrekening>();
        this.lidSinds = lidSinds;
    }

    public Lid(String name, String address, Date lidSinds) {
        this.personen = new ArrayList<Persoon>();
        this.personen.add(new Persoon(this, name));
        this.address = address;
        this.lidSinds = lidSinds;
        this.bankrekeningen = new ArrayList<Bankrekening>();
    }

    public Lid(Long id, String name1, String name2, String address, Date lidSinds) {
        this.id = id;
        this.personen = new ArrayList<Persoon>();
        this.personen.add(new Persoon(this, name1));
        if(name2!=null && !name2.isEmpty()) {
            this.personen.add(new Persoon(this, name2));
        }
        this.address = address;
        this.lidSinds = lidSinds;
        this.bankrekeningen = new ArrayList<Bankrekening>();
    }

    public Lid(String name1, String name2, String address, Date lidSinds) {
        this.personen = new ArrayList<Persoon>();
        this.personen.add(new Persoon(this, name1));
        this.personen.add(new Persoon(this, name2));
        this.address = address;
        this.lidSinds = lidSinds;
        this.bankrekeningen = new ArrayList<Bankrekening>();
    }

    public String toString() {
      return "Lid "+id+" ("+getFirstName()+")";
    }
    
    public String getFirstName() {
        if(personen.size()>=1) return personen.get(0).name;
        return "";
    }
    
    private FactuurContributie contributieFactuur(int jaar) {
        List<FactuurContributie> facturen = 
                FactuurContributie.find
                  .where()
                    .eq("jaar",jaar)
                    .eq("lid", this)
                  .findList();
        if(facturen.isEmpty()) return null;
        if(facturen.size()>1) {
            // FIXME handle error
            System.err.println("ERROR, meer dan 1 factuur voor de contributie van jaar " + jaar + " voor lid " + id + " (" + getFirstName() + ")");
            return null;
        }
        return facturen.get(0);
    }
        
    public boolean contributieBetaald(int jaar) {
        Factuur factuur = contributieFactuur(jaar);
        if (factuur==null) return false;
        return factuur.isBetaald();
    }
    
    public Date contributieBetaaldOp(int jaar) {
        Factuur factuur = contributieFactuur(jaar);
        if (factuur==null) return null;
        if (!factuur.isBetaald()) return null;
        return factuur.betaling.datum;
    }

    public Bankrekening addRekening(String nummer) {
      Bankrekening rek = new Bankrekening(this, nummer);
      bankrekeningen.add(rek);
      return rek;
    }

    public List<Factuur> facturen() {
        List<Factuur> facturen = 
                Factuur.find
                  .where()
                    .eq("lid", this)
                  .findList();
        return facturen;
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
