package com.fourmis.bean;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.*;

import javax.swing.JPanel;

public class Fourmi extends JPanel{
	
	private int id;
	private static final double CHANGE_DIR = 0.005;
	private static final double CHANGE_DIR_RETURN = 0.00;
	private int cx; 					// coordonnée en x
	private int cy; 					// coordonnée en y
	private boolean haveFood = false;	// possède de la nourriture
	private int size = 8;				// taille de la fourmi
	private int sens = 0;				// direction
	private int maxX;					// valeur maximal de la fenêtre en x
	private int maxY;					// valeur maximal de la fenêtre en y
	private int directionX = 0;
	private int directionY = 0;
	private boolean drawBody;
	private static final int N = 1;
	private static final int NE = 2;
	private static final int E = 3;
	private static final int SE = 4;
	private static final int S = 5;
	private static final int SO = 6;
	private static final int O = 7;
	private static final int NO = 8;
	
	Fourmi(int cx, int cy, int maxX, int maxY){
		
		this.cx = cx;
		this.cy = cy;
		this.maxX = maxX;
		this.maxY = maxY;
		
		this.setSize(size, size);
	    this.setPreferredSize(new Dimension(this.size, this.size));
	    
	    this.setOpaque(false);
	}
	
	void move(Fourmiliere fourmiliere, ArrayList<Nourriture> nourritures, HashSet<Pheromone> pheromones, ArrayList<Obstacle> obstacles){
		if(!this.haveFood){
			Pheromone p = new Pheromone(cx+size/2, cy+size/2);
			int centerXFourmiliere = fourmiliere.getCx()+fourmiliere.getWidth()/2-size/2;
			int centerYFourmiliere = fourmiliere.getCy()+fourmiliere.getHeight()/2-size/2;
			boolean findPheromone = false;
			if(pheromones.contains(p) || pheromones.contains(new Pheromone(p.getCx(), p.getCy()-1))
					 || pheromones.contains(new Pheromone(p.getCx()+1, p.getCy()-1))
					 || pheromones.contains(new Pheromone(p.getCx()+1, p.getCy()))
					 || pheromones.contains(new Pheromone(p.getCx()+1, p.getCy()+1))
					 || pheromones.contains(new Pheromone(p.getCx(), p.getCy()+1))
					 || pheromones.contains(new Pheromone(p.getCx()-1, p.getCy()+1))
					 || pheromones.contains(new Pheromone(p.getCx()-1, p.getCy()))
					 || pheromones.contains(new Pheromone(p.getCx()-1, p.getCy()-1))){
				findPheromone = true;
			}
			
			
			// Find phéromone(s)
			if(findPheromone && (cx != centerXFourmiliere || cy != centerYFourmiliere)){
				double distance = 0;
				ArrayList<Integer> directions = new ArrayList<>();
				ArrayList<Double> distances = new ArrayList<>();
				int x = p.getCx();
				int y = p.getCy();
				
				for(int i=-1; i<=1; i++){
					for(int j=-1; j<=1; j++){
						if(!(i == 0 && j == 0)){
							p.setCx(x+i);
							p.setCy(y+j);
							if(pheromones.contains(p)){
								if(i == 0 && j == -1){
									directions.add(N);
								}else if(i == 1 && j == -1){
									directions.add(NE);
								}else if(i == 1 && j == 0){
									directions.add(E);
								}else if(i == 1){
									directions.add(SE);
								}else if(i == 0){
									directions.add(S);
								}else if(j == 1){
									directions.add(SO);
								}else if(j == 0){
									directions.add(O);
								}else {
									directions.add(NO);
								}
								distances.add(distance(p.getCx(), p.getCy(), centerXFourmiliere, centerYFourmiliere));
							}
						}
					}
				}
				
				int saveSens = this.sens;
				
				for(int i=0; i<distances.size(); i++){
					if(distances.get(i) > distance){
						distance = distances.get(i);
						this.sens = directions.get(i);
					}
				}
								
				if(directions.size() <= 1){
					this.sens = saveSens;
				}
				
				updateDirection(fourmiliere, obstacles, true);
				
			}else{
				//Changement de sens aléatoire ou si elle touche le bord de la fenêtre
				boolean changeSens = false;
				directionX = 0;
				directionY = 0;
				if(Math.random() < CHANGE_DIR || cx == 0 || cy == 0 || cx == maxX || cy == maxY || sens == 0){
					changeSens = true;
				}
				
				if(changeSens){
					Random rand = new Random();
					int newSens;
					do{
						newSens = rand.nextInt(8 - 1+1) + 1;
					}while(newSens == this.sens);
					this.sens = newSens;
				}
				
				updateDirection(fourmiliere, obstacles, false);
			}
			
			//Regarde si la fourmi est sur une source de nourriture
			for(Nourriture n : nourritures){
				int centerXFourmi = cx+size/2;
				int centerYFourmi = cy+size/2;
				if(centerXFourmi == n.getCx()+(n.getWidth()/2) && centerYFourmi == n.getCy()+(n.getHeight()/2)){
					this.haveFood = true;
					n.setQuantity(n.getQuantity()-1);
					break;
				}
			}
		}else{
			//Gestion du mouvement de la fourmi dans le cas où elle a de la nourriture
			int centerXFourmiliere = fourmiliere.getCx()+fourmiliere.getWidth()/2-size/2;
			int centerYFourmiliere = fourmiliere.getCy()+fourmiliere.getHeight()/2-size/2;
			int centerXFourmi = cx+size/2;
			int centerYFourmi = cy+size/2;
			
			if(cx != centerXFourmiliere || cy != centerYFourmiliere){
				Pheromone p = new Pheromone(centerXFourmi, centerYFourmi);
				if(pheromones.contains(p)){
					p = getPheromoneByCoord(centerXFourmi, centerYFourmi, pheromones);
					p.setQuantity(p.getQuantity()+100);
				}else{
					pheromones.add(p);
				}
			}
			
			boolean changeSens = false;
			if(Math.random() < CHANGE_DIR_RETURN || sens == 0){
				changeSens = true;
			}
			
			if(changeSens){
				Random rand = new Random();
				this.sens = rand.nextInt(8 - 1+1) + 1;
			}
			
			if(cx != centerXFourmiliere || cy != centerYFourmiliere){
				double minDistance = Double.MAX_VALUE;
				if((distance(cx, cy-1, centerXFourmiliere, centerYFourmiliere) < minDistance && cy > 0 && !changeSens) || (changeSens && sens == N)){
					minDistance = distance(cx, cy-1, centerXFourmiliere, centerYFourmiliere);
					this.sens = N;
				}
				if((distance(cx+1, cy-1, centerXFourmiliere, centerYFourmiliere) < minDistance && cx < maxX && cy > 0 && !changeSens) || (changeSens && sens == NE)){
					minDistance = distance(cx+1, cy-1, centerXFourmiliere, centerYFourmiliere);
					this.sens = NE;
				}
				if((distance(cx+1, cy, centerXFourmiliere, centerYFourmiliere) < minDistance && cx < maxX && !changeSens) || (changeSens && sens == E)){
					minDistance = distance(cx+1, cy, centerXFourmiliere, centerYFourmiliere);
					this.sens = E;
				}
				if((distance(cx+1, cy+1, centerXFourmiliere, centerYFourmiliere) < minDistance && cx < maxX && cy < maxY && !changeSens) || (changeSens && sens == SE)){
					minDistance = distance(cx+1, cy+1, centerXFourmiliere, centerYFourmiliere);
					this.sens = SE;
				}
				if((distance(cx, cy+1, centerXFourmiliere, centerYFourmiliere) < minDistance && cy < maxY && !changeSens) || (changeSens && sens == S)){
					minDistance = distance(cx, cy+1, centerXFourmiliere, centerYFourmiliere);
					this.sens = S;
				}
				if((distance(cx-1, cy+1, centerXFourmiliere, centerYFourmiliere) < minDistance && cx > 0 && cy < maxY && !changeSens) || (changeSens && sens == SO)){
					minDistance = distance(cx-1, cy+1, centerXFourmiliere, centerYFourmiliere);
					this.sens = SO;
				}
				if((distance(cx-1, cy, centerXFourmiliere, centerYFourmiliere) < minDistance && cx > 0 && !changeSens) || (changeSens && sens == O)){
					minDistance = distance(cx-1, cy, centerXFourmiliere, centerYFourmiliere);
					this.sens = O;
				}
				if((distance(cx-1, cy-1, centerXFourmiliere, centerYFourmiliere) < minDistance && cx > 0 && cy > 0 && !changeSens) || (changeSens && sens == NO)){
					this.sens = NO;
				}
				
				updateDirection(fourmiliere, obstacles, false);
				
			}else{
				fourmiliere.setQuantity(fourmiliere.getQuantity()+1);
				haveFood = false;
			}
		}
	}
	
	private void updateDirection(Fourmiliere fourmiliere, ArrayList<Obstacle> obstacles, boolean findPheromone){
		//Gestion du sens de la fourmi
		if (this.sens == N && cy > 0){
			directionX = 0;
			directionY = -1;
		}
		else if(this.sens == NE && cx < maxX && cy > 0){
			directionX = 1;
			directionY = -1;
		}
		else if(this.sens == E && cx < maxX){
			directionX = 1;
			directionY = 0;
		}
		else if(this.sens == SE && cx < maxX && cy < maxY){
			directionX = 1;
			directionY = 1;
		}
		else if(this.sens == S && cy < maxY){
			directionX = 0;
			directionY = 1;
		}
		else if(this.sens == SO && cx > 0 && cy < maxY){
			directionX = -1;
			directionY = 1;
		}
		else if(this.sens == O && cx > 0){
			directionX = -1;
			directionY = 0;
		}
		else if(this.sens == NO && cx > 0 && cy > 0){
			directionX = -1;
			directionY = -1;
		}
		
		int newCx = cx;
		int newCy = cy;
		
		newCx += directionX;
		newCy += directionY;
		
		boolean isCollision = false;
		Object sourceCollision = new Object();
		for(Obstacle o : obstacles){
			if(o instanceof Cercle){
				Cercle c = (Cercle) o;
				if(c.collision(new Cercle(newCx, newCy, this.size))){
					isCollision = true;
					sourceCollision = c;
				}
			}
			if(isCollision){
				break;
			}
		}
		
		if(isCollision && !findPheromone){
			int centerXFourmiliere = fourmiliere.getCx()+fourmiliere.getWidth()/2-size/2;
			int centerYFourmiliere = fourmiliere.getCy()+fourmiliere.getHeight()/2-size/2;
			double distance = 0;
			ArrayList<Integer> directions = new ArrayList<>();
			ArrayList<Double> distances = new ArrayList<>();
			if(this.sens == N){
				Cercle c = (Cercle) sourceCollision;
				if(!c.collision(new Cercle(cx+1, cy, size))){
					directions.add(E);
					distances.add(distance(cx+1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy, size))){
					directions.add(O);
					distances.add(distance(cx-1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy+1, size))){
					directions.add(SO);
					distances.add(distance(cx+1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy+1, size))){
					directions.add(SE);
					distances.add(distance(cx+1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy-1, size))){
					directions.add(NE);
					distances.add(distance(cx+1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy-1, size))){
					directions.add(NO);
					distances.add(distance(cx-1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx, cy+1, size)) && !this.haveFood){
					directions.add(S);
				}

			}else if(this.sens == S){
				Cercle c = (Cercle) sourceCollision;
				if(!c.collision(new Cercle(cx+1, cy, size))){
					directions.add(E);
					distances.add(distance(cx+1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy, size))){
					directions.add(O);
					distances.add(distance(cx-1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy+1, size))){
					directions.add(SO);
					distances.add(distance(cx-1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy+1, size))){
					directions.add(SE);
					distances.add(distance(cx+1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy-1, size))){
					directions.add(NE);
					distances.add(distance(cx+1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy-1, size))){
					directions.add(NO);
					distances.add(distance(cx-1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx, cy-1, size)) && !this.haveFood){
					directions.add(N);
				}
			}else if(this.sens == E){
				Cercle c = (Cercle) sourceCollision;
				if(!c.collision(new Cercle(cx, cy+1, size))){
					directions.add(S);
					distances.add(distance(cx, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx, cy-1, size))){
					directions.add(N);
					distances.add(distance(cx, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy+1, size))){
					directions.add(SO);
					distances.add(distance(cx-1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy+1, size))){
					directions.add(SE);
					distances.add(distance(cx+1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy-1, size))){
					directions.add(NE);
					distances.add(distance(cx+1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy-1, size))){
					directions.add(NO);
					distances.add(distance(cx-1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy, size)) && !this.haveFood){
					directions.add(O);
				}
			}else if(this.sens == O){
				Cercle c = (Cercle) sourceCollision;
				if(!c.collision(new Cercle(cx, cy+1, size))){
					directions.add(S);
					distances.add(distance(cx, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx, cy-1, size))){
					directions.add(N);
					distances.add(distance(cx, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy+1, size))){
					directions.add(SO);
					distances.add(distance(cx-1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy+1, size))){
					directions.add(SE);
					distances.add(distance(cx+1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy-1, size))){
					directions.add(NE);
					distances.add(distance(cx+1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy-1, size))){
					directions.add(NO);
					distances.add(distance(cx-1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy, size)) && !this.haveFood){
					directions.add(E);
				}
			}else if(this.sens == NE){
				Cercle c = (Cercle) sourceCollision;
				if(!c.collision(new Cercle(cx, cy+1, size))){
					directions.add(S);
					distances.add(distance(cx, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx, cy-1, size))){
					directions.add(N);
					distances.add(distance(cx, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy, size))){
					directions.add(E);
					distances.add(distance(cx+1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy, size))){
					directions.add(O);
					distances.add(distance(cx-1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy+1, size))){
					directions.add(SE);
					distances.add(distance(cx+1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy-1, size))){
					directions.add(NO);
					distances.add(distance(cx-1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy+1, size)) && !this.haveFood){
					directions.add(SO);
				}
			}else if(this.sens == SE){
				Cercle c = (Cercle) sourceCollision;
				if(!c.collision(new Cercle(cx, cy+1, size))){
					directions.add(S);
					distances.add(distance(cx, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx, cy-1, size))){
					directions.add(N);
					distances.add(distance(cx, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy, size))){
					directions.add(E);
					distances.add(distance(cx+1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy, size))){
					directions.add(O);
					distances.add(distance(cx-1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy+1, size))){
					directions.add(SO);
					distances.add(distance(cx-1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy-1, size))){
					directions.add(NE);
					distances.add(distance(cx+1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy-1, size)) && !this.haveFood){
					directions.add(NO);
				}
			}else if(this.sens == SO){
				Cercle c = (Cercle) sourceCollision;
				if(!c.collision(new Cercle(cx, cy+1, size))){
					directions.add(S);
					distances.add(distance(cx, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx, cy-1, size))){
					directions.add(N);
					distances.add(distance(cx, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy, size))){
					directions.add(E);
					distances.add(distance(cx+1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy, size))){
					directions.add(O);
					distances.add(distance(cx-1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy+1, size))){
					directions.add(SE);
					distances.add(distance(cx+1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy-1, size))){
					directions.add(NO);
					distances.add(distance(cx-1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy-1, size)) && !this.haveFood){
					directions.add(NE);
				}
			}else if(this.sens == NO){
				Cercle c = (Cercle) sourceCollision;
				if(!c.collision(new Cercle(cx, cy+1, size))){
					directions.add(S);
					distances.add(distance(cx, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx, cy-1, size))){
					directions.add(N);
					distances.add(distance(cx, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy, size))){
					directions.add(E);
					distances.add(distance(cx+1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy, size))){
					directions.add(O);
					distances.add(distance(cx-1, cy, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx-1, cy+1, size))){
					directions.add(SO);
					distances.add(distance(cx-1, cy+1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy-1, size))){
					directions.add(NE);
					distances.add(distance(cx+1, cy-1, centerXFourmiliere, centerYFourmiliere));
				}
				if(!c.collision(new Cercle(cx+1, cy+1, size)) && !this.haveFood){
					directions.add(SE);
				}
			}
			
			Random rand = new Random();
			if(directions.size() > 0){
				int direction = 0;
				if(!this.haveFood){
					direction = directions.get(rand.nextInt(directions.size()));
				}else{
					distance = Double.MAX_VALUE;
					for(int i=0; i<distances.size(); i++){
						if(distances.get(i) < distance){
							distance = distances.get(i);
							direction = directions.get(i);
						}
					}
				}
				this.sens = direction;
			}
			
		}
		
		if(this.sens == N && cy > 0){
			directionX = 0;
			directionY = -1;
		}
		else if(this.sens == NE && cx < maxX && cy > 0){
			directionX = 1;
			directionY = -1;
		}
		else if(this.sens == E && cx < maxX){
			directionX = 1;
			directionY = 0;
		}
		else if(this.sens == SE && cx < maxX && cy < maxY){
			directionX = 1;
			directionY = 1;
		}
		else if(this.sens == S && cy < maxY){
			directionX = 0;
			directionY = 1;
		}
		else if(this.sens == SO && cx > 0 && cy < maxY){
			directionX = -1;
			directionY = 1;
		}
		else if(this.sens == O && cx > 0){
			directionX = -1;
			directionY = 0;
		}
		else if(this.sens == NO && cx > 0 && cy > 0){
			directionX = -1;
			directionY = -1;
		}
		
		cx += directionX;
		cy += directionY;

	}
	
	public void draw(Graphics g){		
		g.setColor(Color.black);
		
		if(drawBody){
			if(directionX == 0 && directionY == -1){
				g.fillOval(cx+size/2-4, cy+size, 8, 16);
			}else if(directionX == 1 && directionY == -1){
				int posX = cx+2;
				int posY = cy+size-2;
				int[] x = {posX-1, posX-4, posX-6, posX-10, posX-11, posX-11, posX-7, posX-5, posX-1, posX, posX};
				int[] y = {posY, posY, posY+1, posY+5, posY+8, posY+11, posY+11, posY+10, posY+6, posY+4, posY+2};
				g.fillPolygon(x, y, x.length);
			}else if(directionX == 1 && directionY == 0){
				g.fillOval(cx-16, cy+size-8, 16, 8);
			}else if(directionX == 1 && directionY == 1){
				int posX = cx+2;
				int posY = cy+2;
				int[] x = {posX, posX, posX-1, posX-5, posX-8, posX-10, posX-10, posX-9, posX-5, posX-3, posX-1};
				int[] y = {posY-1, posY-4, posY-6, posY-10, posY-11, posY-11, posY-7, posY-5, posY-1, posY, posY};
				g.fillPolygon(x, y, x.length);
			}else if(directionX == 0 && directionY == 1){
				g.fillOval(cx+size/2-4, cy-16, 8, 16);
			}else if(directionX == -1 && directionY == 1){
				int posX = cx+size-2;
				int posY = cy+2;
				int[] x = {posX, posX, posX+1, posX+5, posX+7, posX+11, posX+11, posX+10, posX+6, posX+4, posX+1};
				int[] y = {posY-1, posY-3, posY-5, posY-9, posY-10, posY-10, posY-8, posY-5, posY-1, posY, posY};
				g.fillPolygon(x, y, x.length);
			}else if(directionX == -1 && directionY == 0){
				g.fillOval(cx+size, cy+size/2-4, 16, 8);
			}else if(directionX == -1 && directionY == -1){
				int posX = cx+size-2;
				int posY = cy+size-2;
				int[] x = {posX+1, posX+3, posX+5, posX+9, posX+10, posX+10, posX+8, posX+5, posX+1, posX, posX};
				int[] y = {posY, posY, posY+1, posY+5, posY+7, posY+11, posY+11, posY+10, posY+6, posY+4, posY+1};
				g.fillPolygon(x, y, x.length);
			}
		}
		
		g.fillOval(cx, cy, 8, 8);
		if(this.isHaveFood())
			g.setColor(Color.red);
		else
			g.setColor(Color.darkGray);
		g.fillOval(cx+2,  cy+2, 4, 4);
		
	}
	
	boolean collisionRect(Rectangle r2){
		Rectangle r1 = new Rectangle(cx, cy, this.getWidth(), this.getHeight());
		
		return r1.intersects(r2);
	}
	
	private double sqr(double a) {
		return a*a;
	}
	 
	private double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(sqr(y2 - y1) + sqr(x2 - x1));
	}
	
	private Pheromone getPheromoneByCoord(int cx, int cy, HashSet<Pheromone> pheromones){
		for (Pheromone p : pheromones) {
			if (p.getCx() == cx && p.getCy() == cy) {
				return p;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Fourmi fourmi = (Fourmi) o;
		return id == fourmi.id &&
				cx == fourmi.cx &&
				cy == fourmi.cy &&
				haveFood == fourmi.haveFood &&
				size == fourmi.size &&
				sens == fourmi.sens &&
				maxX == fourmi.maxX &&
				maxY == fourmi.maxY &&
				directionX == fourmi.directionX &&
				directionY == fourmi.directionY &&
				drawBody == fourmi.drawBody;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, cx, cy, haveFood, size, sens, maxX, maxY, directionX, directionY, drawBody);
	}

	private boolean isHaveFood() {
		return haveFood;
	}

	void setDrawBody(boolean drawBody) {
		this.drawBody = drawBody;
	}

	void setId(int id) {
		this.id = id;
	}
	
}
