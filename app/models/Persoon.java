package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;

import org.apache.commons.mail.EmailException;

@Entity
public class Persoon extends Model {

    @GeneratedValue(strategy=GenerationType.AUTO, generator="persoon_seq_gen")
    @SequenceGenerator(name="persoon_seq_gen", sequenceName="PERSOON_SEQ")
    @Id
    public Long id;
    
    @ManyToOne
    public Lid lid;
    
    @Constraints.Required
    public String name;
    
    public String email;
    
    @OneToMany(cascade=CascadeType.ALL, mappedBy="persoon", orphanRemoval = true)
    @javax.persistence.OrderBy("id ASC")
    public List<EmailCheckProbe> probes;

    public Persoon(Lid lid, String name) {
        this.lid = lid; 
        this.name = name;
    }

    public Persoon(Lid lid, String name, String email) {
        this.lid = lid; 
        this.name = name;
        this.email = email;
    }

    public Date emailLastVerified() {
      String sql = "select max(r.timestamp) as last "+
        "from email_check_probe p "+
        "  join probe_response r on r.probe_id=p.id "+
        "where p.persoon_id=:persoonid and "+
        "p.email=:email";
    
      SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
      sqlQuery.setParameter("persoonid", this.id);
      sqlQuery.setParameter("email", this.email);

      SqlRow row = sqlQuery.findUnique();

      return row.getDate("last");
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
