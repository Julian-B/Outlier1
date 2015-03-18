package org.usfirst.frc.team5687.robot.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;

import org.usfirst.frc.team5687.robot.Constants;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 * Autonomous command group driven by a script uploaded to the roborio
 */
public class AutonomousScript extends CommandGroup {
	private String scriptName = null;
	private boolean isValid = false;

	public  AutonomousScript(String fullPath, String scriptName) {
		isValid = true;
		this.scriptName = scriptName;
		// Open the file...
		try {
			// Get a BufferedReader
			File file = new File(fullPath);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String scriptLine;
			// For each line in file...
			int line = 1;
			while ((scriptLine = bufferedReader.readLine()) != null) {
				scriptLine = scriptLine.trim().toLowerCase();
				while (scriptLine.contains("  ")) {
					scriptLine = scriptLine.replace("  ", " ");
				}

				// If the line is not blank and does not start with a #, parse a command
				if (scriptLine.length()>0 && scriptLine.charAt(0)!='#') {
					// Add the command to the sequence
					Command command = ParseLine(scriptLine);
					if (command!=null) {
						addSequential(command);
					} else {
						// Log the bad line to RoboRio
						LogError(String.format("Parse error on line %1$d: %2$s", line, scriptLine));
						isValid = false;
					}
				}
				line++;
			}
			fileReader.close();
		} catch (IOException e) {
			LogError(String.format("IO error parsing %1$s: %2$s", scriptName, e.getMessage()));
		}

	}

	private Command ParseLine(String scriptLine) {
		// Guard for null
		if (scriptLine==null) { return null; }
		String work = scriptLine.trim().toLowerCase();
		while (work.contains("  ")) {
			work = work.replace("  ", " ");
		}

		// Guard for empty lines
		if (scriptLine.length()==0) { return null; }

		// Grab the tokens
		String[] tokens = scriptLine.split(" ");

		if ("drive".equals(tokens[0])) {
			return ParseDriveCommand(tokens);
		} else if ("move".equals(tokens[0])) {
			return ParseMoveCommand(tokens);
		} else if ("turn".equals(tokens[0]) 
				|| "rotate".equals(tokens[0])) {
			return ParseTurnCommand(tokens);
		} else if ("wait".equals(tokens[0])) {
			return ParseWaitCommand(tokens);
		}

		return null;
	}
	private Command ParseDriveCommand(String[] tokens) {
		// drive direction distance [units [at speed]]
		int direction = 0;
		double inches = 0;
		double speed = 0;

		int tokenCheck = 3;

		if ("forward".equals(tokens[1])) {
			direction = 1;
		} else if ("back".equals(tokens[1])|| "backwards".equals(tokens[1])) {
			direction = -1;
		}

		try {
			inches = Double.parseDouble(tokens[2]);
		} catch (NumberFormatException nfe) {
			return LogError("Invalid distance passed to drive command: " + tokens[2]);
		}

		if (tokenCheck<tokens.length && "inches".equals(tokens[tokenCheck])) {
			tokenCheck ++;
		}

		if (tokenCheck<tokens.length && "at".equals(tokens[tokenCheck])) {
			tokenCheck ++;
		}

		if (tokenCheck<tokens.length && "speed".equals(tokens[tokenCheck])) {
			tokenCheck ++;
		}

		if (tokenCheck<tokens.length){
			try {
				speed = Double.parseDouble(tokens[tokenCheck]);
			} catch (NumberFormatException nfe) {
				return LogError("Invalid speed passed to drive command: " + tokens[tokenCheck]);
			}
		}
		else {
			speed = Constants.AutonomousSettings.DRIVE_SPEED;
		}










		return new AutoDrive(speed * direction, inches);
	}


	private Command ParseMoveCommand(String[] tokens) {
		// move stacker to setpoint 
		// move stacker to inches

		if (tokens.length<2 || !"stacker".equals(tokens[1])) { 
			return LogError("Move command requires the phrase 'move stacker to'."); 
		}

		if (tokens.length<3 || !"to".equals(tokens[2])) {
			return LogError("Move command requires the phrase 'move stacker to'."); 
		}

		if (tokens.length<4) {
			return LogError("Move command requires a setpoint or height."); 
		}		


		double setpoint = -1;
		try {
			setpoint = Double.parseDouble(tokens[3]) - Constants.StackerHeights.HEIGHT_OFFSET;
		} catch (NumberFormatException nfe) {
			setpoint = -1;
		}

		if (setpoint < 0) {
			// Try to parse as a setpoint
			try {
				setpoint = ParseDoubleConstant(Constants.StackerHeights.class, tokens[3]);
			} catch (ParseException pe) {
				setpoint = -1;
			}
		}

		if (setpoint < 0) {
			return LogError("Unable to determine height or setpoint for move stacker command: " + tokens[3]);
		}
		return new MoveStackerToSetpoint(setpoint); 
	}


	private Command ParseTurnCommand(String[] tokens) {
		// turn direction degrees [units]
		int direction = 0;
		if (tokens.length<2) { 
			return LogError("No direction passed to turn command."); 
		}
		if ("left".equals(tokens[1])) {
			direction = Rotate.LEFT;
		} else if ("right".equals(tokens[1])) {
			direction = Rotate.RIGHT;
		} else {
			// Log parse error
			return LogError("Invalid direction passed to turn command: " + tokens[1]);
		}

		if (tokens.length<3) { 
			return LogError("No degrees passed to turn command."); 
		}

		double degrees = 0;
		try {
			degrees = Double.parseDouble(tokens[2]);
		} catch (NumberFormatException nfe) {
			return LogError("Invalid degrees passed to turn command: " + tokens[2]);
		}



		return new Rotate(direction, degrees);
	}

	private Command ParseWaitCommand(String[] tokens) {
		// wait seconds
		double waitTime = 0;
		int tokenCheck = 2;

		if (tokens.length<2) { 
			return LogError("Wait command requires a time to wait"); 
		}	
		// optional addition of phrase "for" (wait FOR 3)
		if ("for".equals(tokens[2])){
			tokenCheck ++;
		}
		//is it a number?
		try {
			waitTime = Double.parseDouble(tokens[tokenCheck]);
		} catch (NumberFormatException nfe) {
			return LogError("Invalid time passed to wait command: " + tokens[tokenCheck]);
		}
		// if the unit is seconds, 
		//multiply by 1000 to reach the milliseconds unit required
		if ("seconds".equals(tokens[tokenCheck +1])||"second".equals(tokens[tokenCheck +1]) ) {
			waitTime = waitTime * 1000;
		}
		//no units besides seconds/milliseconds accepted!
		else if (!"milliseconds".equals(tokens[tokenCheck +1])) {
			return LogError("Only units milliseconds/seconds accepted in wait command" + tokens[tokenCheck+1]);
		}

		return new AutonomousWait((int) waitTime);

	}

	private Command LogError(String message) {
		// Log error
		DriverStation.reportError(message, false);
		return null;
	}

	private double ParseDoubleConstant(Class constantClass, String constantName) throws ParseException {
		try {
			Field field = constantClass.getField(constantName.toUpperCase());
			if ((field.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) > 0) {
				Object val = field.get(null);
				if (val instanceof Double) {
					return ((Double)val).doubleValue();
				} else {
					throw new ParseException("Unable to convert " + constantName + " to a value in class " + constantClass.getName(), 0);
				}
			} else {
				throw new ParseException("Unable to convert " + constantName + " to a value in class " + constantClass.getName(), 0);
			}
		} catch (NoSuchFieldException nsfe) {
			throw new ParseException("Unable to convert " + constantName + " to a value in class " + constantClass.getName(), 0);
		} catch (IllegalAccessException iae) {
			throw new ParseException("Unable to convert " + constantName + " to a value in class " + constantClass.getName(), 0);
		}
	}

	private int ParseIntConstant(Class constantClass, String constantName) throws ParseException {
		try {
			Field field = constantClass.getField(constantName.toUpperCase());
			if ((field.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) > 0) {
				Object val = field.get(null);
				if (val instanceof Integer) {
					return ((Integer)val).intValue();
				} else {
					throw new ParseException("Unable to convert " + constantName + " to a value in class " + constantClass.getName(), 0);
				}
			} else {
				throw new ParseException("Unable to convert " + constantName + " to a value in class " + constantClass.getName(), 0);
			}
		} catch (NoSuchFieldException nsfe) {
			throw new ParseException("Unable to convert " + constantName + " to a value in class " + constantClass.getName(), 0);
		} catch (IllegalAccessException iae) {
			throw new ParseException("Unable to convert " + constantName + " to a value in class " + constantClass.getName(), 0);
		}
	}

	public String DislayName() {
		return scriptName + (isValid ? " - OK" : " - ERROR");
	}
}
