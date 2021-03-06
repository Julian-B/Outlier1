package org.usfirst.frc.team5687.robot.commands;

import java.util.Calendar;
import java.util.Date;

import org.usfirst.frc.team5687.robot.Constants;
import org.usfirst.frc.team5687.robot.Robot;
import org.usfirst.frc.team5687.robot.subsystems.DriveTrain;
import org.usfirst.frc.team5687.robot.subsystems.Stacker;

import edu.wpi.first.wpilibj.command.Command;

/**
 * Command to drive the robot at specified speeds for specified times or distances. 
 */
public class AutoWait extends Command {

	DriveTrain drive = Robot.driveTrain;
	private Calendar end = null;
	private int timeToWait = 0;
	

	public AutoWait(int timeToWait) {
		
        this.timeToWait = timeToWait;
    }


    // Called just before this Command runs the first time
    protected void initialize() {
    	end = Calendar.getInstance();
    	end.setTime(new Date());
    	end.add(Calendar.MILLISECOND, timeToWait);
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
	
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
    	Calendar now = Calendar.getInstance();
    	now.setTime(new Date());
    	return !now.before(end);
    }

    // Called once after isFinished returns true
    protected void end() {
    
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
