package models;

import siena.*;

public class Participant extends Model {

  @Id(Generator.AUTO_INCREMENT)
  public Long id;
  
  @Column("mail_address")
  public String mailAddress;
  

  public static Query<Participant> all() {
    return Model.all(Participant.class);
  }

}
  
  

