package models;

import java.util.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;
import play.*;

import com.avaje.ebean.*;
import com.avaje.ebean.validation.Email;

import org.apache.commons.mail.EmailException;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;

@Entity
public class Persoon extends Model implements Subject { 

    public final static String LID_ROLE="lid";
    public final static String BESTUUR_ROLE="bestuur";
    public final static String ADMIN_ROLE="admin";
    public final static String PENNINGMEESTER_ROLE="penningmeester";

    private final static String[] ALL_ROLES = {LID_ROLE, BESTUUR_ROLE, ADMIN_ROLE, PENNINGMEESTER_ROLE};

    @GeneratedValue(strategy=GenerationType.AUTO, generator="persoon_seq_gen")
    @SequenceGenerator(name="persoon_seq_gen", sequenceName="PERSOON_SEQ")
    @Id
    public Long id;
    
    @ManyToOne
    public Lid lid;
    
    @Constraints.Required
    public String name;

    public String email;
    
    @OneToMany(mappedBy="persoon")
    @javax.persistence.OrderBy("id ASC")
    public List<EmailCheckProbe> probes;

    // Login name for web application
    public String accountName;

    public String cryptedPassword;

    @ManyToMany
    public List<SecurityRole> roles = new ArrayList<SecurityRole>();
    
    public Persoon(Lid lid, String name) {
        this.lid = lid; 
        this.name = name;
    }

    public Persoon(Lid lid, String name, String email) {
        this.lid = lid; 
        this.name = name;
        this.email = email;
    }

    public Persoon(String name, String accountName) {
        this.name = name;
        this.accountName = accountName;
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

    /* Deadbolt Subject methods */
    public List<? extends Role> getRoles() {
      return roles;
    }

    public List<? extends Permission> getPermissions() {
      return null;
    }

    @Override
    public String getIdentifier()
    {
        return accountName;
    }

    public static Persoon findByAccountName(String account)
    {
        if (account==null || account.isEmpty()) return null;
        return find.where()
                   .eq("accountName", account)
                   .findUnique();
    }

    private static String getSalt() {
       int size = 16;
       byte[] bytes = new byte[size];
       new Random().nextBytes(bytes);
       return org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(bytes);
    }

    private static synchronized String encrypt(String plaintext) throws Exception {
      MessageDigest md = null;
      try {
        md = MessageDigest.getInstance("SHA"); //step 2
      } catch(NoSuchAlgorithmException e) {
        throw new Exception(e.getMessage());
      }
      try {
        md.update(plaintext.getBytes("UTF-8")); //step 3
      } catch(UnsupportedEncodingException e) {
        throw new Exception(e.getMessage());
      }
   
      byte raw[] = md.digest(); //step 4
      String hash = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(raw);
      return hash; //step 6
    }

    public static String encryptPassword(String plain) throws Exception {
        String salt = getSalt();
        return "CP01::"+salt+"::"+encrypt(salt+plain);
    }

    public static boolean verifyPassword(String encrypted, String plain) throws Exception {
        String[] parts = encrypted.split("::");
        if(parts.length!=3) return false;
        String version = parts[0];
        String salt = parts[1];
        String hash = parts[2];
        if (!version.equals("CP01")) return false;
        return hash.equals(encrypt(salt+plain)); 
    }

    public void setPassword(String plainPassword) throws Exception {
        this.cryptedPassword = encryptPassword(plainPassword);
    }

    public static Persoon authenticate(String account, String plainPassword) throws Exception {
        Persoon p = findByAccountName(account);
        if (p==null) return null;
        if (p.cryptedPassword==null || p.cryptedPassword.isEmpty()) {
          Logger.debug("No password set, hash would be " + encryptPassword(plainPassword)); 
          return null;
        }
        if (Persoon.verifyPassword(p.cryptedPassword, plainPassword)) {
           return p;
        } else {
          return null;
        }
    }
    
    public static List<String> allRoles() {
        List<String> result = java.util.Arrays.asList(ALL_ROLES);
        return result;
    }

    public static void seedSecurityRoles() {
        for(String role: ALL_ROLES) {
            if (SecurityRole.findByName(role)==null) {
              SecurityRole r = new SecurityRole(role);
              r.save();
            }
        }
    }

    public static void seedAdmin(String password) {
        Persoon admin = new Persoon("admin", "admin");
        try {
            admin.setPassword(password);
            admin.addRole(ADMIN_ROLE);
            admin.save();
        } catch (Exception e) {
            Logger.error("Unable to create initial admin user", e);
        }
    }

    public boolean hasRole(String role) {
        for(SecurityRole r: roles) {
            if(r.getName().equals(role)) return true;
        } 
        return false;
    }

    public boolean hasAnyRole(String... neededRoles) {
        for(SecurityRole r: roles) {
            for(String n: neededRoles) {
                if(r.getName().equals(n)) return true;
            }
        } 
        return false;
    }

    public void addRole(String role) {
        SecurityRole r = SecurityRole.findByName(role);
        if (r!=null) {
            this.roles.add(r);
        }
    }

    public void setRoles(Set<String> roles) {
        this.roles = new ArrayList<SecurityRole>(roles.size());
        for(String role: roles) {
            addRole(role);
        }
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
