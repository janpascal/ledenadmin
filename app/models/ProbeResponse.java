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
public class ProbeResponse extends Model {

    public enum ResponseType {
      @EnumValue("WEBLINK")
      WEBLINK,
      @EnumValue("IMGVIEW")
      IMGVIEW,
      @EnumValue("MANUAL")
      MANUAL
    }
    
    @GeneratedValue(strategy=GenerationType.AUTO, generator="response_seq_gen")
    @SequenceGenerator(name="response_seq_gen", sequenceName="RESPONSE_SEQ")
    @Id
    public Long id;

    @ManyToOne
    public EmailCheckProbe probe;
    
    @Formats.DateTime(pattern="dd/MM/yyyy hh:mm:ss")
    public Date timestamp;
    public ResponseType type;
   
    public ProbeResponse(EmailCheckProbe probe, ResponseType type) {
        this.probe = probe;
        this.timestamp = new Date();
        this.type = type;
    }

    public String toString() {
      return "ProbeResponse "+id+" for probe "+probe.token+" for person " +probe.persoon.name;
    }

    public static void create(ProbeResponse response) {
        response.save();
    }

    public static void delete(Long id) {
       find.ref(id).delete();
    }
     
    public static Finder<Long,ProbeResponse> find = new Finder<Long, ProbeResponse>(
        Long.class, ProbeResponse.class
    );

}
