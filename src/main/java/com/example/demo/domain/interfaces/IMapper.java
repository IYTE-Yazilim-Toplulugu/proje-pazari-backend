package com.example.demo.domain.interfaces;

public interface IMapper<TSource, TDestination> {
    /// <summary>
    /// Maps the source object to the destination type.
    /// </summary>
    /// <typeparam name="TSource">The type of the source object.</typeparam>
    /// <typeparam name="TDestination">The type of the destination
    /// object.</typeparam>
    /// <param name="source">The source object to map from.</param>
    /// <returns>An instance of the destination type.</returns>
    TDestination Map(TSource source);

    /// <summary>
    /// Maps the source object to the destination type and returns it as an object.
    /// </summary>
    /// <param name="source">The source object to map from.</param>
    /// <param name="destinationType">The type of the destination object.</param>
    /// <returns>An instance of the destination type as an object.</returns>
    TDestination Map(TSource source, TDestination destinationType);
}
