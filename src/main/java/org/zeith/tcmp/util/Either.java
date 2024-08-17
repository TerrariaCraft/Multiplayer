package org.zeith.tcmp.util;

import java.util.Optional;

public abstract class Either<L, R>
{
	public static <L, R> Either<L, R> left(L l)
	{
		return new Either.Left<>(l);
	}
	
	public static <L, R> Either<L, R> right(R l)
	{
		return new Either.Right<>(l);
	}
	
	public abstract Optional<L> left();
	
	public abstract Optional<R> right();
	
	public static class Left<L, R>
			extends Either<L, R>
	{
		private final Optional<L> left;
		
		public Left(L left)
		{
			this.left = Optional.of(left);
		}
		
		@Override
		public Optional<L> left()
		{
			return left;
		}
		
		@Override
		public Optional<R> right()
		{
			return Optional.empty();
		}
	}
	
	public static class Right<L, R>
			extends Either<L, R>
	{
		private final Optional<R> right;
		
		public Right(R right)
		{
			this.right = Optional.of(right);
		}
		
		@Override
		public Optional<L> left()
		{
			return Optional.empty();
		}
		
		@Override
		public Optional<R> right()
		{
			return right;
		}
	}
}