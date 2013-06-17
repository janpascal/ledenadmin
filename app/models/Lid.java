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
    @Constraints.MinLength(value=1)
    @Constraints.MaxLength(value=2)
    public List<Persoon> personen;
    
    @Constraints.Required
    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date lidSinds;
    
    public Date lidTot;
    
    public String address;
   
    @OneToMany(mappedBy="lid")
    public List<Bankrekening> rekeningnummers;

    public Lid(String name, Date lidSinds) {
        this.personen = new ArrayList<Persoon>();
        this.personen.add(new Persoon(name));
        this.rekeningnummers = new ArrayList<Bankrekening>();
        this.lidSinds = lidSinds;
    }

    public Lid(String name, String address, Date lidSinds) {
        this.personen = new ArrayList<Persoon>();
        this.personen.add(new Persoon(name));
        this.address = address;
        this.lidSinds = lidSinds;
        this.rekeningnummers = new ArrayList<Bankrekening>();
    }

    public Lid(Long id, String name1, String name2, String address, Date lidSinds) {
        this.id = id;
        this.personen = new ArrayList<Persoon>();
        this.personen.add(new Persoon(name1));
        this.personen.add(new Persoon(name2));
        this.address = address;
        this.lidSinds = lidSinds;
        this.rekeningnummers = new ArrayList<Bankrekening>();
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

    public Bankrekening addRekening(String rekeningnummer) {
      Bankrekening rek = new Bankrekening(this, rekeningnummer);
      rekeningnummers.add(rek);
      Bankrekening.create(rek);
      return rek;
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
