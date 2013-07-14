package models;

import java.util.*;
import java.net.URL;

import javax.persistence.*;

import play.Play;
import play.Configuration;
import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;
import com.avaje.ebean.annotation.*;

@Entity
public class EmailCheckProbe extends Model {

    public enum ProbeType {
      @EnumValue("EMAILPROBE")
      EMAIL
    }
    
    @GeneratedValue(strategy=GenerationType.AUTO, generator="probe_seq_gen")
    @SequenceGenerator(name="probe_seq_gen", sequenceName="PROBE_SEQ")
    @Id
    public Long id;

    @ManyToOne
    public Persoon persoon;
    
    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date timestamp;
    public String email; 
    public String token;
    public ProbeType type;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="probe")
    @javax.persistence.OrderBy("id ASC")
    public List<ProbeResponse> responses;

    public EmailCheckProbe(Persoon persoon, ProbeType type) {
        this.persoon = persoon;
        this.email = persoon.email;
        this.timestamp = new Date();
        this.token = UUID.randomUUID().toString();
        this.type = type;
        this.responses = new ArrayList<ProbeResponse>();
    }

    public ProbeResponse addResponse(ProbeResponse.ResponseType responseType) {
        ProbeResponse response = new ProbeResponse(this, responseType);
        responses.add(response);
        response.save();
        return response;
    }

    public boolean isAnswered() {
        return !responses.isEmpty();
    }

    public String toString() {
      return "EmailCheckProbe "+id+" ("+persoon.name+")";
    }

    public static void create(EmailCheckProbe probe) {
        probe.save();
    }

    public static void delete(Long id) {
       find.ref(id).delete();
    }
     
    public static Finder<Long,EmailCheckProbe> find = new Finder<Long, EmailCheckProbe>(
        Long.class, EmailCheckProbe.class
    );

}
