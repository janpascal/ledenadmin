package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;
import com.avaje.ebean.annotation.PrivateOwned;
import org.apache.commons.mail.EmailException;

@Entity
public class Lid extends Model {

    @GeneratedValue(strategy=GenerationType.AUTO, generator="lid_seq_gen")
    @SequenceGenerator(name="lid_seq_gen", sequenceName="LID_SEQ")
    @Id
    public Long id;
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="lid")
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

    public Lid() {
        this.personen = new ArrayList<Persoon>();
        this.bankrekeningen = new ArrayList<Bankrekening>();
    }

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
    
    private Factuur contributieFactuur(int jaar) {
        List<Factuur> facturen = 
                Factuur.find
                  .where()
                    .eq("type", Factuur.FACTUUR_CONTRIBUTIE)
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

    public boolean lidInJaar(int year) {
      Date jan1 = new GregorianCalendar(year,0,1,0,0).getTime();
      Date dec31 = new GregorianCalendar(year+1,0,1,0,0).getTime();
      return ( (lidSinds==null) || (lidSinds.before(dec31)) )
          && ( (lidTot==null) || (lidTot.after(jan1)));
    }

    public boolean isHuidigLid() {
      return (lidTot==null) || (lidTot.after(new Date()));
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
    
    public List<Factuur> contributieFacturen() {
        List<Factuur> facturen = 
                Factuur.find
                  .where()
                    .eq("lid", this)
                    .eq("type", Factuur.FACTUUR_CONTRIBUTIE) 
                  .order("jaar")
                  .findList();
        return facturen;
    }

    public static Lid findLid(String name, String bankrekening) {
        if (bankrekening!=null && !bankrekening.isEmpty()) {
            Lid lid = Lid.find.where()
                .eq("bankrekeningen.nummer", bankrekening)
                .findUnique();
            if (lid != null) return lid;
        }

        return Lid.find.where()
            .icontains("personen.name", name)
            .findUnique();
    }
    
    public static void create(Lid lid) {
        lid.save();
    }

    public static void delete(Long id) {
       find.ref(id).delete();
    }
/*
    @Override
    public void update() {
      for(Persoon p: personen) {
        p.update();
      }
      for(Bankrekening r: bankrekeningen) {
        r.update();
      }
      super.update();
    }
*/
     
    public static Finder<Long,Lid> find = new Finder<Long, Lid>(
            Long.class, Lid.class
          );
}
