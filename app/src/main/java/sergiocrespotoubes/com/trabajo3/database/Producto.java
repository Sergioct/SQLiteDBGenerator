package sergiocrespotoubes.com.trabajo3.database;

public class Producto{

	public Producto(){}

	private long id;

	public long getId(){return id;}

	public void setId(long id){this.id = id;}

	private String nombre;

	public String getNombre(){return nombre;}

	public void setNombre(String nombre){this.nombre = nombre;}

	private double coste;

	public double getCoste(){return coste;}

	public void setCoste(double coste){this.coste = coste;}

}