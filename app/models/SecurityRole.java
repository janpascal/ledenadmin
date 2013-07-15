package models;

import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;
import com.avaje.ebean.validation.*;
import be.objectify.deadbolt.core.models.Role;

@Entity
public class SecurityRole extends Model implements Role { 

    @GeneratedValue(strategy=GenerationType.AUTO, generator="sec_role_seq_gen")
    @SequenceGenerator(name="sec_role_seq_gen", sequenceName="SECURITY_ROLE_SEQ")
    @Id
    public Long id;

    @Constraints.Required
    public String name;

    public SecurityRole(String name) {
      this.name = name;
    }

    public static final Finder<Long, SecurityRole> find = 
                new Finder<Long, SecurityRole>(Long.class, SecurityRole.class);

    public String getName()
    {
        return name;
    }

    public static SecurityRole findByName(String name)
    {
        return find.where()
                   .eq("name",
                       name)
                   .findUnique();
    }
}
