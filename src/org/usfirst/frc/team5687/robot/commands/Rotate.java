package org.usfirst.frc.team5687.robot.commands;

import java.util.Calendar;
import java.util.Date;

import org.usfirst.frc.team5687.robot.Constants;
import org.usfirst.frc.team5687.robot.Constants.Calibration;
import org.usfirst.frc.team5687.robot.Robot;
import org.usfirst.frc.team5687.robot.subsystems.DriveTrain;
import org.usfirst.frc.team5687.robot.subsystems.Stacker;

import edu.wpi.first.wpilibj.command.Command;

/**
 * Command to rotate left or right for a particular time or for a specific degrees
 */
public class Rotate extends Command {
	public static int LEFT = -1;
	public static int RIGHT = 1;

	DriveTrain drive = Robot.driveTrain;
	private Calendar end = null;
	private int timeToRotate = 0;
	private double rightSpeed = 0;
	private double leftSpeed = 0;
    
	/**
	 * Rotate left or right the specified number of degrees.
	 * @param direction
	 * @param degrees
	 */
	public Rotate(int direction, double degrees) {
        this(direction, (int)Math.round(degrees / Constants.AutonomousSettings.DRIVE_SPEED * Calibration.ROTATION));
    }

	/**
	 * Rotate left or right for the specified number of milliseconds. 
	 * @param direction
	 * @param milliseconds
	 */
	public Rotate(int direction, int milliseconds) {
        requires(drive);
        // Calculate the settings
        this.leftSpeed = Constants.AutonomousSettings.DRIVE_SPEED * direction == LEFT ? -1 : 1;
        this.rightSpeed = Constants.AutonomousSettings.DRIVE_SPEED * direction == RIGHT ? -1 : 1;
        
        this.timeToRotate = milliseconds;
    }
	
    // Called just before this Command runs the first time
    protected void initialize() {
    	end = Calendar.getInstance();
    	end.setTime(new Date());
    	end.add(Calendar.MILLISECOND, timeToRotate);
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
		drive.tankDrive(leftSpeed, rightSpeed, false);
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
    	Calendar now = Calendar.getInstance();
    	now.setTime(new Date());
    	return !now.before(end);
    }

    // Called once after isFinished returns true
    protected void end() {
    	drive.tankDrive(0,0,false);
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
