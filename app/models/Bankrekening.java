package models;

import java.util.*;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

@Entity
public class Bankrekening extends Model {

    @GeneratedValue(strategy=GenerationType.AUTO, generator="bankrek_seq_gen")
    @SequenceGenerator(name="bankrek_seq_gen", sequenceName="BANKREKENING_SEQ")
    @Id
    public Long id;

    @ManyToOne
    public Lid lid;
    
    public String rekeningnummer;

    public Bankrekening(Lid lid, String nummer) {
        this.lid = lid;
        this.rekeningnummer = nummer;
    }

    public static void create(Bankrekening bankrekening) {
        bankrekening.save();
    }

    public static void delete(Long id) {
       find.ref(id).delete();
    }
     
    public static Finder<Long,Bankrekening> find = new Finder<Long, Bankrekening>(
            Long.class, Bankrekening.class
          );
}
