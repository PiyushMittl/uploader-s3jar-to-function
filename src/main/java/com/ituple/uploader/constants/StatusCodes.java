package com.ituple.uploader.constants;

public class StatusCodes {

	private StatusCodes() {
	}

	public static final int	OK							= 200;
	public static final int	SOME_INTERNAL_ERROR_CODE	= 500;
	public static final int	BAD_REQUEST					= 400;
	public static final int	UNAUTHORIZED_FOR_TENANT		= 402;
	public static final int	UNAUTHORIZED_FOR_IAM		= 401;
	public static final int	NO_DATA						= 405;

}
