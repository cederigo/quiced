package models;

import siena.*;

public class Participant extends Model {

  @Id(Generator.AUTO_INCREMENT)
  public Long id;
  
  @Column("mail_address")
  public String mailAddress;
  
  @Column("name")
  public String name;
  
  @Column("answer")
  public int answer = -1;
  

  public static Query<Participant> all() {
    return Model.all(Participant.class);
  }

}
  
  

