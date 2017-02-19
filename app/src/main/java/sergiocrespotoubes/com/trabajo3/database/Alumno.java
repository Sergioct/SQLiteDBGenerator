package sergiocrespotoubes.com.trabajo3.database;

public class Alumno{

	public Alumno(){}

	private long id;

	public long getId(){return id;}

	public void setId(long id){this.id = id;}

	private String nombre;

	public String getNombre(){return nombre;}

	public void setNombre(String nombre){this.nombre = nombre;}

	private String dni;

	public String getDni(){return dni;}

	public void setDni(String dni){this.dni = dni;}

	private double nota;

	public double getNota(){return nota;}

	public void setNota(double nota){this.nota = nota;}

	private int chico;

	public int getChico(){return chico;}

	public void setChico(int chico){this.chico = chico;}

}